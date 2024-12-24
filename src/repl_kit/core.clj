(ns repl-kit.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [seesaw.core :refer [pack!
                                 show!
                                 frame
                                 config!
                                 listen]]
            [seesaw.rsyntax :refer [text-area]]
            [seesaw.dev :refer [show-events show-options]])
  (:import [org.fife.ui.rtextarea RTextScrollPane])
  (:gen-class))


(defn mk-frame []
  (let [f (frame :title "REPL-KIT" 
                 :visible? true
                 :width 200
                 :height 200
                 :on-close :exit)
        ops {:show        (fn [] (-> f show!))
             :set-content (fn [content]
                            (config! f :content content)
                            content)}]
    (fn [operation & args] (-> (ops operation) (apply args)))))

(defn mk-txt-area []
  '())


(defn mk-app []
  (let [fm (mk-frame)
        ta (text-area :syntax :clojure :editable? true)
        sp (RTextScrollPane. ta true)
        ops {:get-text (fn [] (.getText ta))
             :show     (fn [] (fm :show))
             :get-ta   (fn [] ta)}]
    (fm :set-content sp) 
    (listen ta #{:key-typed :property-change} (fn [e] (prn (bean e))))
    (fn [operation & args] (-> (ops operation) (apply args)))))

(defn -main [& args]
  (-> (mk-app) 
      :show))

(comment
  *e
  (def app (mk-app)) 
  (app :show)
  (app :get-text)
  (.getCaretOffsetFromLineStart ta) 
  (show-events (app :get-ta))
  (show-options ta) 

  
  (load-file)
  (show-events (frame))
  (show-events (seesaw.rsyntax/text-area :syntax :clojure :editable? true))
  (show-options (seesaw.rsyntax/text-area :syntax :clojure :editable? true))
  ;;
  )
