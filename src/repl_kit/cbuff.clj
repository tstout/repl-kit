(ns repl-kit.cbuff)


(defn mk-cbuff 
  "Create a constrained buffer. The returned fn accepts the following
   options:

   :forward    - Move index forward in the buffer, returns item at index
   :backward   - Move index backward in the buffer, returns item at index
   :push       - Add an item to the buffer, returns buffer
   
   Forward will increment until end of buffer reached.
   Backward will decrement until beginning of buffer reached.
   Push adds an item to the buffer (if it does not already exist)."  
  []
  (let [index (atom 0) 
        buff  (atom [])
        ops   {:forward  (fn [] 
                           (if (empty? @buff) nil
                               (do (reset! 
                                    index 
                                    (min (inc @index) (dec (count @buff))))
                                   (nth @buff @index))))
               :backward (fn []
                           (if (empty? @buff) nil
                               (do (reset!
                                    index
                                    (max (dec @index) 0))
                                   (nth @buff @index))))
               :push     (fn [x] 
                           (when-not (some #{x} @buff)
                             (swap! buff conj x)))}]   
    (fn [operation & args] (-> (ops operation) (apply args)))))


(comment 
  (def buff (mk-cbuff))

  (buff :push "one")
  (buff :push "two")
  (buff :push "three")
  (buff :push "four")
  (buff :forward)
  (buff :backward)

  ;;
  )