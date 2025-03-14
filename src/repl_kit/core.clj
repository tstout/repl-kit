(ns repl-kit.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [repl-kit.theme :refer [apply-dark-theme]]
            [repl-kit.key-map :refer [configure-key-map]]
            [repl-kit.repl-eval :refer [repl-init]] 
            [seesaw.core :refer [show!
                                 frame
                                 label
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

(def notes 
  "For showing results of REPL evaluation consider using 
   Graphics.drawText(String text, int x, int y)
   Need to see how to translate txt position in RSyntaxTextArea to 
   coordinates")


(defn tokens 
  "Acquire seq of current parse tokens for the text Area"
  [ta]
  (-> ta
      .getDocument
      .iterator
      iterator-seq))

(defn caret-coords [ta]
  (let [{:keys [x y]} (-> ta 
                          bean 
                          :caret
                          bean)]
    {:x x :y y}))

(defn mk-frame []
  (let [f   (frame :title "(REPL-KIT)" 
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
  (let [fr        (mk-frame)
        top-label (label :text "")
        lw        (log-window :auto-scroll? true 
                              :background  :black  
                              :foreground :white)
        ta        (text-area :syntax :clojure 
                             :editable? true 
                             :minimum-size [2048 :by 2048])
        sp        (mig-panel :constraints ["" "[][][grow]" "[][50%][50%]"]
                             :border [(line-border :thickness 1) 5]
                             :items [[ top-label "span, grow"]
                                     [ (RTextScrollPane. ta true) "span, grow"]
                                     #_[ :separator         "growx, wrap"]
                                     [ (scrollable lw)     "span, grow"]])
        ops       {:get-text  (fn [] (.getText ta))
                   :show      (fn [] (fr :show))
                   :load-file (fn [file] (.setText ta (slurp file)))
                   :get-ta    (fn [] ta)
                   :log       (fn [msg] (log lw msg))
                   :clear-log (fn [] (clear lw))} 
        repl-conn (repl-init)]
    (fr :set-content sp)
    (apply-dark-theme ta)
    (configure-key-map {:txt-area   ta 
                        :log-window lw
                        :top-label  top-label
                        :repl-conn  repl-conn}) 
    #_(listen ta #{:key-typed :property-change} (fn [e] (prn (bean e))))
    (log lw "REPL-KIT v1.0.5\n")
    (fn [operation & args] (-> (ops operation) (apply args)))))


(defn -main [& args]
  (-> (mk-app) 
      :show))

(comment
  *e
  (def app (mk-app))

  ;; hello

  (app :get-text)
  (app :show)
  (app :get-text)
  
  
  

  
  (tokens ta)
  (.getDocument ta)
  (bean ta)
  (bean (app :get-ta))

  (.getParserCount ta)


  (-> (app :get-ta) bean :highlighter bean)
  (type (app :get-ta))

  *ns*

  (-> .getX (.getCaret (app :get-ta)))

  (bean (:caret (bean (app :get-ta))))
  
  (-> :get-ta
      app
      .getCaret 
      .getDot)

  (caret-coords (app :get-ta))

  (.getCaretOffsetFromLineStart (app :get-ta))
  (show-events (app :get-ta))
  (show-options (app :get-ta))

  (app :load-file "pom.xml")


  (str *ns*)

  (.setText (app :get-ta) (slurp "deps.edn"))


  (range 10)

  (app :log "Hello..\n")
  (app :clear-log)
  (load-file)
  (show-events (frame))
  (show-options (log-window))
  (show-events (seesaw.rsyntax/text-area :syntax :clojure :editable? true)) 
  (show-options (seesaw.rsyntax/text-area :syntax :clojure :editable? true))
  ;;
  )
