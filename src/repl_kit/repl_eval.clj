(ns repl-kit.repl-eval
  (:import [clojure.lang LineNumberingPushbackReader]
           [java.io StringReader 
            PipedReader 
            PipedWriter 
            BufferedReader 
            PrintWriter 
            InputStreamReader]
           [java.net Socket])
  (:require [clojure.core.server :as server]
            [clojure.edn :as edn]))

;; First pass at this...
;; -- Simple algorithm --
;; When ctrl-enter keypress:
;; * Extract Coordinates of caret position from text area
;; * parse backwards from caret position until character is 
;;   not a closing paren, keeping count of number of closing parens
;; * Once 

(defn start-repl-server []
  (server/start-server
   {:accept  'clojure.core.server/io-prepl 
    :address "localhost"
    :port    5555
    :name    "my-prepl"
    :args    [:valf identity]}))


;; (defn eval-form [code]
;;   (with-open [reader (PipedReader.) 
;;               writer (PipedWriter.)]
;;     (.connect reader writer)
;;     (let [result! (promise)]
;;       (future
;;         (server/remote-prepl
;;          "localhost" "5555"
;;          reader
;;          (fn [msg]
;;            (deliver result! msg))
;;          :valf identity))
;;       (.write writer code)
;;       (.flush writer)
;;       @result!)))

;; (defmacro ^:private thread
;;   [^String name daemon & body]
;;   `(doto (Thread. (fn [] ~@body) ~name)
;;      (.setDaemon ~daemon)
;;      (.start)))


;; (defn repl-eval [writer code state]
;;   (.write writer code)
;;   (.flush writer)
;;   @@state)


;; (defn mk-repl-conn [host port]
;;   (let [reader   (PipedReader.)
;;         writer   (PipedWriter.)
;;         state    (atom (promise))
;;         repl-fut (future
;;                    (server/remote-prepl
;;                     host 
;;                     port
;;                     reader
;;                     (fn [msg]
;;                       (deliver @state msg))
;;                     :valf identity)) 
;;         ops      {:close (fn [] nil)
;;                   :eval  (fn [code]
;;                            (reset! state (promise))
;;                            (.write writer code)
;;                            (.flush writer)
;;                            @@state)}]
;;     (.connect reader writer)
;;     (fn [operation & args] (-> (ops operation) (apply args)))))


(defn connect-to-prepl [host port]
  (let [socket (Socket. host port)
        writer (PrintWriter. (.getOutputStream socket) true)
        reader (BufferedReader. (InputStreamReader. (.getInputStream socket)))]
    {:socket socket
     :writer writer
     :reader reader}))

(defn send-form [connection form]
  (let [{:keys [writer]} connection]
    (.println writer (if (string? form) 
                       form 
                       (pr-str form))))) 

(defn read-response [connection]
  (let [{:keys [reader]} connection]
    (when-let [line (.readLine reader)]
      (edn/read-string {:default tagged-literal} line)))) 

(defn do-eval [conn code]
  (send-form conn code)
  (read-response conn))

(defn mk-evaluator []
  
  )


(comment
  
  ;; TODO cleanup all
 *default-data-reader-fn* 
 *data-readers*
 (start-repl-server) 
 (def repl-conn (connect-to-prepl "localhost" 5555))

 (edn/read-string {:default tagged-literal} "{:val #namespace[user]}")
  
 (edn/read-string {:default tagged-literal}
                  (pr-str {:tag :ret, :val *ns* :ns "user", :ms 0, :form "*ns*"}))

 (do-eval repl-conn '(+ 20 20))

 (do-eval repl-conn "(+ 20 20)")

 (do-eval repl-conn  "*ns*")
 (do-eval repl-conn '(in-ns repl-kit.repl-eval))
 
 (pr-str *ns*)

 (pr-str '(in-ns 'repl-kit.core))
 (in-ns  'repl-kit.repl-eval)

 (pr-str 'repl-conn)

 :hello

 *ns*
 (eval "(+ 20 20")
 (ns-publics 'repl-kit.core)
 (ns-publics 'repl-kit.repl-eval)
  ;;
 )