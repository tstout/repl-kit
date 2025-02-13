(ns repl-kit.key-map
  (:require [seesaw.keymap :refer [map-key]]
            [repl-kit.form-parse :refer [form-txt]]
            [repl-kit.repl-eval :refer [do-eval]]
            [seesaw.keystroke :refer [keystroke]]
            [seesaw.chooser :refer [choose-file]]
            [seesaw.widgets.log-window :refer [log]]))

;; TODO - need to determine best approach to set classpath
;; after loading a file, perhaps just do it when deps.edn loaded.
;; look at new 1.12.0 lib features
;; https://clojure.org/news/2024/09/05/clojure-1-12-0

(defn configure-key-map [m]
  (let [{:keys [txt-area log-window repl-conn]} m
        state                                   (atom {:dirty false
                                                       :file  nil})]
    (map-key txt-area
             "control S"
             (fn [_]
               (when-let [file (@state :file)]
                 (log log-window (format "saving file %s\n" file))
                 (spit file (.getText txt-area)))))
    
    (map-key txt-area 
             "control O" 
             (fn [_] 
               (choose-file :success-fn (fn [_ file] 
                                          (let [path (.getAbsolutePath file)]
                                            (swap! state assoc :file path)
                                            (log log-window (format "loading file: %s\n" path))
                                            (.setText txt-area (slurp path)))))))
    (map-key txt-area
             "control ENTER"
             (fn [_]
               (let [txt    (form-txt txt-area)
                     result (do-eval repl-conn txt)]
                 (log log-window (format "%s]\n" txt))
                 (log log-window (format "%s> %s\n" (:ns result) (pr-str result))))))))


(comment 
  (keystroke "control O")
  ;;
  )