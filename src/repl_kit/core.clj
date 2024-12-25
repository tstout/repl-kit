(ns repl-kit.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [seesaw.core :refer [show!
                                 frame
                                 text
                                 vertical-panel
                                 config!
                                 listen
                                 scrollable
                                 ]]
            [seesaw.widgets.log-window :refer [log-window log clear]]
            [seesaw.border :refer [line-border]]
            [seesaw.rsyntax :refer [text-area]]
            [seesaw.mig :refer [mig-panel]]
            [seesaw.dev :refer [show-events show-options]])
  (:import [org.fife.ui.rtextarea RTextScrollPane])
  (:gen-class))

(defn mk-frame []
  (let [f (frame :title "REPL-KIT" 
                 :visible? true
                 :width 800
                 :height 800
                 :on-close :exit)
        ops {:show        (fn [] (-> f show!))
             :set-content (fn [content]
                            (config! f :content content)
                            content)}]
    (fn [operation & args] (-> (ops operation) (apply args)))))

(defn mk-app 
  "Main application closure. Operations available from the returned fn:
   :get-ta - acquire reference to the RSyntaxTextArea
   :get-text - String containing current text area content"
  []
  (let [fr (mk-frame)
        lw (log-window)
        ta (text-area :syntax :clojure :editable? true :minimum-size [2048 :by 2048])
        sp (mig-panel :constraints ["" "[][grow]" "[80%][20%]"]
                      :border [(line-border :thickness 1) 5]
                      :items [[ (RTextScrollPane. ta true) "span, grow"]
                              [ :separator         "growx, wrap"]
                              [ (scrollable lw)     "span, grow"]]) 
        ops {:get-text (fn [] (.getText ta))
             :show     (fn [] (fr :show))
             :get-ta   (fn [] ta)
             :log (fn [msg] (log lw msg))
             :clear-log (fn [] (clear lw))}]
    (fr :set-content sp) 
    (listen ta #{:key-typed :property-change} (fn [e] (prn (bean e))))
    (log lw "REPL-KIT V1.0.4\n")
    (fn [operation & args] (-> (ops operation) (apply args)))))

(defn -main [& args]
  (-> (mk-app) 
      :show))

(comment
  *e
  (def app (mk-app)) 
  (app :show)
  (app :get-text)
  (.getCaretOffsetFromLineStart (app :get-ta)) 
  (show-events (app :get-ta))
  (show-options (app :get-ta)) 

  (config! (app :get-ta) :content "This ia  a test")
  (.setText (app :get-ta) (slurp "deps.edn"))
  (app :log "Hello..\n")
  (app :clear-log)
  (load-file)
  (show-events (frame))
  (show-events (seesaw.rsyntax/text-area :syntax :clojure :editable? true))
  (show-options (seesaw.rsyntax/text-area :syntax :clojure :editable? true))
  ;;
  )
