(ns repl-kit.ns-util)


(defn ls-ns []
  (->> (all-ns)
       (map ns-name)
       (map name)))

(defn ns-exists? [ns]
  (not (->> (ls-ns)
            (filter #(= % ns))
            empty?)))

(defn publics [ns] 
  (keys (ns-publics ns)))



(comment
  ;; test
  
  *e
  
  (ns-exists? "repl-kit.ns-util")
  (ls-ns)
  (publics)

  (count (ls-ns))

  (keys (ns-publics 'repl-kit.ns-util))
  (in-ns 'repl-kit.ns-util)

  ;;
  )