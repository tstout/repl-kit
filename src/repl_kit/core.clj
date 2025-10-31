(ns repl-kit.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [clojure.string :as string]
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
                                 top-bottom-split]]
            [seesaw.widgets.log-window :refer [log-window log clear]]
            [seesaw.border :refer [line-border]] 
            [seesaw.rsyntax :refer [text-area]] 
            [seesaw.font :refer [font-families font]]
            [seesaw.mig :refer [mig-panel]]
            [seesaw.dev :refer [show-events show-options]])
  (:import [org.fife.ui.rtextarea RTextScrollPane])
  (:gen-class))

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

(defn mk-split-pane [ta lw]
  (top-bottom-split
   (RTextScrollPane. ta true)
   (scrollable lw)
   :divider-location 350
   :one-touch-expandable? true))

(defn mk-app 
  "Main application closure. Operations available from the returned fn:
   :get-ta - acquire reference to the RSyntaxTextArea
   :get-text - String containing current text area content"
  []
  (let [fr        (mk-frame)
        top-label (label :text "")
        ns-label  (label :text "*ns* user")
        a-label   (label :text "")
        lw        (log-window :auto-scroll? true 
                              :background  :black  
                              :foreground :white)
        ta        (text-area :syntax :clojure 
                             :font (font :name :monospaced
                                         :size 14)
                             :editable? true 
                             :minimum-size [2048 :by 2048])
        sp        (mig-panel 
                   :constraints ["" "[][grow][]" "[][100%][]"]
                   :border [(line-border :thickness 1) 5]
                   :items [[top-label "span, grow"]  
                           [(mk-split-pane ta lw) "span, grow"]
                           [ns-label "span 1"]
                           [a-label "span 1"]
                           #_[(RTextScrollPane. ta true) "span, grow"]
                           #_[:separator         "growx, wrap"]
                           #_[(scrollable lw)     "span, grow"]])
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
                        :ns-label   ns-label
                        :top-label  top-label
                        :a-label    a-label
                        :repl-conn  repl-conn}) 
    #_(listen ta #{:key-typed :property-change} (fn [e] (prn (bean e))))
    #_(log lw "REPL-KIT v1.0.5\n")
    (log lw (-> "help.txt" io/resource slurp))
    (fn [operation & args] (-> (ops operation) (apply args)))))


(defn -main [& args]
  (-> (mk-app) 
      :show))

(comment
  *e
  *1
  (def app (mk-app))

  (app :get-text)
  (app :show)
  (app :get-text) `*ns*

  (range 10)
  (app :log "Hello..\n")
  (app :clear-log)
  (load-file)
  (seesaw.font/font)
  (seesaw.font/font-families)
  (show-events (frame))
  (show-options (log-window))
  (show-events (seesaw.rsyntax/text-area :syntax :clojure :editable? true))
  (show-options (seesaw.rsyntax/text-area :syntax :clojure :editable? true))
  (bean (seesaw.rsyntax/text-area :syntax :clojure :editable? true))

  (font-families)
  (seesaw.font/font "ARIAL-ITALIC-20")
  (seesaw.font/font "MENLO-MONOSPACE-14")

  (->> (font-families) 
       (filter #(string/includes? % "mon")))

  ;;
  )
