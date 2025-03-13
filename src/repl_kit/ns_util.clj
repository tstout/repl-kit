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

  (count (ls-ns))

  (keys (ns-publics 'repl-kit.ns-util))
  (in-ns 'repl-kit.ns-util)

  (+ 6 35 26 24 82 55 46 56 52 48 27 15)

  (/ 330 60)

  (/ 472 60)

  


  
  ;;
  )