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
   :d-frm   3   ;; Data form
   :error   4}) ;; Some error 

(def state-matrix
  "This matrix maps events to next-state/action pairs based on current state.
   nil values are interpreted as no-ops."
  {; event name  :init                 :p-frm             :o-form         :d-frm             :error
   :close-paren [[:p-frm form-end]     [nil form-end]     [nil nil]       [nil nil]          [nil nil]]
   :open-paren  [[:p-frm form-start]   [nil form-start]   [nil nil]       [nil nil]          [nil nil]]
   :ws-char     [[nil nil]             [nil nil]          [nil form-end]  [nil nil]          [nil nil]]
   :other-char  [[:o-frm form-start]   [nil nil]          [nil nil]       [nil nil]          [nil nil]]})

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
    #_(prn "current state is:" (cur-state context))
    #_(prn "next state is: " next-state)
    (when action (action context))
    next-state))

(defn find-form-start [txt init-offset]
  (let [ctxt (atom {:init-offset   init-offset
                    :current-state :init
                    :form-start    0
                    :form-end      0
                    :offset        init-offset})] 
    (doseq [c      (reverse (subs txt 0 init-offset))
            :while (not (form-start-found? ctxt))]
      (->> (invoke-action 
            ctxt 
            state-matrix 
            (case c
              \(       :open-paren
              \)       :close-paren
              \tab     :ws-char
              \space   :ws-char
              \,       :ws-char
              \newline :ws-char
              :other-char))
           (set-state ctxt))
      (dec-offset ctxt))
    ctxt))

(defn form-txt
  "Given a text area, determine the top-most form based on current cursor
   position."
  [txt-area]
  (let [dot         (-> txt-area
                        .getCaret
                        .getDot) 
        txt         (.getText txt-area)
        f-info      (find-form-start (.getText txt-area) dot)
        offset      (:offset @f-info)
        init-offset (:init-offset @f-info)]
    (subs txt offset init-offset)))



(comment
  (def ctxt (atom {:offset 20}))

  (find-form-start "[] (+ 20 20)" 12)

  (time (find-form-start "[] (+ 20 20)" 12))

  (def expr "abc (+ 20 20)")
  (def expr "  *ns*")
  (def expr "{:a 1 :b 2}")
  
  (find-form-start expr (.length expr))

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