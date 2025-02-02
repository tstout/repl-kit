(ns repl-kit.key-map
  (:require [seesaw.keymap :refer [map-key]]
            [repl-kit.form-parse :refer [form-txt]]
            [repl-kit.repl-eval :refer [do-eval]]
            [seesaw.keystroke :refer [keystroke]]
            [seesaw.chooser :refer [choose-file]]
            [seesaw.widgets.log-window :refer [log]]))

#_(fn [file] (.setText ta (slurp file)))


;; TODO - need to determine best approach to set classpath
;; after loading a file, perhaps just do it when deps.edn loaded.

(defn configure-key-map [m]
  (let [{:keys [txt-area log-window repl-conn]} m]
    ;; (map-key txt-area
    ;;          "control S"
    ;;          )

    (map-key txt-area 
             "control O" 
             (fn [_] 
               (log log-window "control-o\n")
               (choose-file :success-fn (fn [fc file] 
                                          #_(log log-window (format "File Type: %s - %s\n" 
                                                                  (type file)
                                                                  (.getAbsolutePath file)))
                                          (.setText txt-area (slurp (.getAbsolutePath file)))))))
    (map-key txt-area
             "control ENTER"
             (fn [_]
               (let [txt    (form-txt txt-area)
                     result (do-eval repl-conn (form-txt txt-area))]
                 (log log-window (format "%s]\n" txt))
                 (log log-window (format "%s> %s\n" (str *ns*) (pr-str result))))))))


(comment 
  (keystroke "control O")
  ;;
  )