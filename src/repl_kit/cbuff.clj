(ns repl-kit.cbuff)


(defn mk-cbuff 
  "Create a constrained circular buffer. The returned fn accepts the following
   options:

   :forward    - Move index forward in the buffer
   :backward   - Move index backward in the buffer
   :push       - Add an item to the buffer"  
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
               :push     (fn [x] (swap! buff conj x))}]   
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