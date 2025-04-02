(ns repl-kit.key-map
  (:import [javax.swing KeyStroke])
  (:require [seesaw.keymap :refer [map-key]] 
            [clojure.java.io :as io]
            [repl-kit.form-parse :refer [form-txt]]
            [zprint.core :as zp]
            [repl-kit.repl-eval :refer [do-eval]]
            [repl-kit.cbuff :refer [mk-cbuff]]
            [seesaw.keystroke :refer [keystroke]]
            [seesaw.chooser :refer [choose-file]]
            [seesaw.widgets.log-window :refer [log clear]]))



(def fmt-opts
  {:width  100
   :map    {:comma?   false
            :sort?    true
            :indent   2
            :justify? true}
   :list   {:indent 1 
            :hang? false}
   :vector {:indent 1} 
   :style  [:community]})


(defn configure-key-map [m]
  (let [{:keys [txt-area log-window repl-conn top-label]} m
        cbuff                                             (mk-cbuff)
        state                                             (atom {:dirty false
                                                                 :file  nil})
        log-w                                             (partial log log-window)]
    (map-key txt-area
             "control F"
             (fn [_]
               (let [rstr (zp/zprint-file-str
                           (.getText txt-area)
                           nil
                           fmt-opts)]
                 (.setText txt-area rstr)
                 (log-w "Formatting file...\n"))))
    
    (map-key txt-area
             "control A" 
             (fn [_] 
               (when-let [path (cbuff :forward)]
                 (.setText txt-area (slurp path))
                 (swap! state assoc :file path)
                 (.setText top-label path))))
    
    (map-key txt-area
             "control Z"
             (fn [_] 
               (when-let [path (cbuff :backward)]
                 (.setText txt-area (slurp path))
                 (swap! state assoc :file path)
                 (.setText top-label path))))
    
    (map-key txt-area
             "control C"
             (fn [_]
               (clear log-window)))

    (map-key txt-area
             "control H"
             (fn [_]
               (log-w (-> "help.txt" 
                          io/resource 
                          slurp))))

    (map-key txt-area
             "control L"
             ;; TODO - when loading entire file, need to change ns to the
             ;; file. Is using file name sufficient to determine ns?
             (fn [_]
               (when-let [file (@state :file)]
                 (log-w (format "eval file %s\n" file))
                 (let [result (do-eval
                               repl-conn
                               (format "(load-file \"%s\")" file))]
                   (log-w (format "%s> %s\n"
                                  (:ns result)
                                  (pr-str (:val result))))))))

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
                                            (.setText txt-area (slurp path))
                                            (cbuff :push path))))))

    (map-key txt-area
             "control ENTER"
             (fn [_]
               (let [txt    (form-txt (.getText txt-area)
                                      (-> txt-area
                                          .getCaret
                                          .getDot))
                     result (do-eval repl-conn txt)]
                 (log-w (format "%s]\n" txt))
                 (log-w (format "%s> %s\n"
                                (:ns result)
                                (zp/zprint-file-str
                                 (:val result)
                                 nil
                                 fmt-opts))))))))

(comment
  (zp/zprint-str (slurp "/Users/tstout/src/sample-proj/deps.edn"))

  (def fstr (zp/zprint-file-str
             (slurp "/Users/tstout/src/sample-proj/deps.edn")
             nil
             nil
             nil))

  (spit "fmt.edn" fstr)
  

  
  

  (when-not )

  (->> (all-ns)
       (map ns-name)
       (map name))
  ;;
  )