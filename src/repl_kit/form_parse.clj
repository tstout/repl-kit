(ns repl-kit.form-parse)


(defn dec-offset [ctxt]
  (swap! ctxt #(assoc 
                % 
                :offset 
                (dec (@ctxt :offset)))))

(defn inc-form-start [ctxt]
  (swap! ctxt #(assoc
                 %
                 :form-start
                 (inc (@ctxt :form-start)))))

(defn inc-form-end [ctxt]
  (swap! ctxt #(assoc
                 %
                 :form-end
                 (inc (@ctxt :form-end)))))

(defn form-start-found? [ctxt]
  (let [{:keys [form-start form-end]} @ctxt]
    (and (= form-start form-end) (not= form-end 0))))

(defn form-start [ctxt]
  (inc-form-start ctxt))

(defn form-end [ctxt]
  (inc-form-end ctxt))


(def states
  "Maps state names to matrix index"
  {:init    0   ;; Initial
   :p-frm   1   ;; Paren form
   :o-frm   2   ;; Other form (string, constant, var, etc)
   :error   3}) ;; Some error 

(def state-matrix
  "This matrix maps events to next-state/action pairs based on current state.
   nil values are interpreted as no-ops."
  {; event name   :init                  :p-frm        :o-form       :error
   :close-paren [[:p-frm form-end]     [nil nil]     [nil nil]     [nil nil]]
   :open-paren  [[:p-frm form-start]   [nil nil]     [nil nil]     [nil nil]]
   :ws-char     [[nil nil]             [nil nil]     [nil nil]     [nil nil]]
   :other-char  [[nil nil]             [nil nil]     [nil nil]     [nil nil]]
   })

(defn set-state
  "Replace current state with new-state, if new-state is not nil"
  [ctx new-state]
  (when new-state
    (swap! ctx assoc :current-state new-state)))

(defn cur-state [ctx]
  (:current-state @ctx))

(defn state-pair
  "Returns the next-state/action pair corresponding to the current state."
  [evt-name ctx matrix]
  (nth
   (evt-name matrix)
   ((cur-state ctx) states)))

(defn- invoke-action
  "According to the supplied state matrix, invoke the appropriate action
  and return the next state."
  [context matrix evt-name]
  (let [[next-state action] (state-pair evt-name context matrix)]
    (prn "current state is:" (cur-state context))
    (prn "next state is: " next-state)
    (when action (action context))
    next-state))

(defn driver [txt init-offset]
  (let [ctxt (atom {:init-offset   init-offset
                    :current-state :init
                    :form-start    0
                    :form-end      0
                    :offset        init-offset})] 
    (doseq [c      (reverse (subs txt 0 init-offset))
            :while (not (form-start-found? ctxt))]
      (invoke-action ctxt 
                     state-matrix 
                     (case c
                       \(    :open-paren
                       \)    :close-paren
                       \tab   :ws-char
                       \space :ws-char
                       \,     :ws-char
                       :other-char))
      (dec-offset ctxt))
    ctxt))


;; (defn evt-handler
;;   "Listen for events on the :main-bus topic. Push the events though the state matrix
;;   triggering the appropriate behavior."
;;   []
;;   (let [sub-ch (sub-evt :main-bus :sm-ch)
;;         context (atom {:animation-ch  nil
;;                        :current-state :idle})]
;;     (go-loop
;;      []
;;       (let [evt (<! sub-ch)]
;;         (prn (str "RX SM event: " (evt-name evt)))
;;         (->>
;;          (invoke-action context state-matrix evt)
;;          (set-state context))
;;         (recur)))))


(comment
  (def ctxt (atom {:offset 20}))

  (driver "[] (+ 20 20)" 11)

  (reverse "(+ 20 20)")
  \)
  (nth "012345" 5)
  (dec-offset ctxt)

  (reverse (subs "(+ 20 20)" 0 8))

  (subs "01234567" 0 8)

  (seq "abc")

  (doseq [c "abc"]
    (prn c))



  ;;
  )