(ns repl-kit.options
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]))

(def cli-options
  [;; First three strings describe a short-option, long-option with optional
   ;; example argument description, and a description. All three are optional
   ;; and positional.
   ["-p" "--port PORT" "REPL port"         
    :default 5555
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-s" "--server SERVER"  "REPL server(host)" 
    :default "localhost"]
   ["-i" "--install" "install clojure (future feature)"]
   ["-h" "--help"    "show help"]])

(defn usage [options-summary]
  (->> ["repl-kit - basic editor with integrated REPL" 
        "Options:"
        options-summary
        ""]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with an error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options _arguments errors summary] :as opts} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)} 
      :else 
      opts)))


(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn process-args 
  "Process command line arguments. Either exits the program
   with an error message or returns a map of options."
  [args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      options)))
     

(comment
  (validate-args ["-s" "stout-pi4" "--install"])

  (validate-args ["-p" "5000" "-s" "stout-pi4"])

  (process-args ["-p" "5000" "-s" "stout-pi4"])

  (validate-args [])

  ;;
)


