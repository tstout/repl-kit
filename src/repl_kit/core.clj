(ns repl-kit.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [seesaw.core :refer [pack! show! frame]])
  (:gen-class))

(defn -main [& args] 
  (-> (frame :title "Hello" :content "Hi there") 
      pack! 
      show!))
