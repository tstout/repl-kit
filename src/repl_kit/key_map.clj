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
  (let [{:keys [txt-area log-window repl-conn top-label]} m
        state                                   (atom {:dirty false
                                                       :file  nil})
        log-w (partial log log-window)] 
    (map-key txt-area
             "control E"
             ;; TODO - when loading entire file, need to change ns to the
             ;; file. Is using file name sufficient to determine ns?
             (fn [_]
               (when-let [file (@state :file)]
                 (log-w (format "eval file %s\n" file))
                 (let [result (do-eval 
                               repl-conn 
                               (format "(load-file \"%s\")" file))]
                   (log-w (format "%s> %s\n" (:ns result) (pr-str result)))))))

    (map-key txt-area
             "control S"
             (fn [_]
               (when-let [file (@state :file)]
                 (log-w (format "saving file %s\n" file))
                 (spit file (.getText txt-area)))))
    
    (map-key txt-area 
             "control O" 
             (fn [_] 
               (choose-file :success-fn (fn [_ file] 
                                          (let [path (.getAbsolutePath file)]
                                            (swap! state assoc :file path)
                                            (.setText top-label path)
                                            (log-w (format "loading file: %s\n" path))
                                            (.setText txt-area (slurp path)))))))
    ;; TODO - do a pretty print of the result here
    (map-key txt-area
             "control ENTER"
             (fn [_]
               (let [txt    (form-txt txt-area)
                     result (do-eval repl-conn txt)]
                 (log-w (format "%s]\n" txt))
                 (log-w (format "%s> %s\n" (:ns result) (pr-str result))))))))


(comment 
  (keystroke "control O")
  ;;
  )