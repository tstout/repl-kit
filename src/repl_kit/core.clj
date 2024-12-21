(ns repl-kit.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [seesaw.core :refer [pack! show! frame]]
            [seesaw.rsyntax :refer [text-area]]
            [seesaw.dev :refer [show-events show-options]])
  (:gen-class))

(defn -main [& args] 
  (-> (frame :title "Hello" 
             :content (text-area :syntax :clojure :editable? true)) 
      pack! 
      show!))


(comment
  (seesaw/rtextarea "")

  (seesaw/rtextarea)

  (load-file)
  (show-events (frame))
  (show-events (seesaw.rsyntax/text-area :syntax :clojure :editable? true))
  (show-options (seesaw.rsyntax/text-area :syntax :clojure :editable? true))
  ;;
  )
