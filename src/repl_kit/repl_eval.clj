(ns repl-kit.repl-eval
  (:import [java.io 
            BufferedReader 
            PrintWriter 
            InputStreamReader]
           [java.net Socket])
  (:require [clojure.core.server :as server]
            [clojure.edn :as edn]))

(defn start-repl-server [opts]
  (let [{:keys [port server]} opts]
  (server/start-server
   {:accept  'clojure.core.server/io-prepl 
    :address "localhost"
    :port    port
    :name    "repl-kit-prepl"})))

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
                       (pr-str form)))
    (.flush writer))) 

(defn read-response [connection]
  (let [{:keys [reader]} connection]
    #_(.flush reader)
    (when-let [line (.readLine reader)]
      (edn/read-string {:default tagged-literal} line)))) 

(defn when-done
  "Invoke a fn when a future completes. Returns a future wrapping the result
   of the fn to call."
  [future-to-watch fn-to-call]
  (future (fn-to-call @future-to-watch)))

(defn do-eval 
  "Evaluate a form in another thread. The provided function is 
   invoked when the evaluation completes. The function is passed
   the result of the evaluation."
  [conn code f]
  {:pre [(fn? f)]}
  #_(println (format "code in eval is '%s'" code))
  (try 
    (when-done (future (send-form conn code)
                       (read-response conn))
               f)
    (catch Exception e
      (println (format "Exception during do-eval: %s" (.getMessage e))))))

(defn repl-init 
  "Startup a prepl server and return a connection to it."
  [opts]
  (let [{:keys [server port remote]} opts]
  (alter-var-root #'*repl* (constantly true)) ;; Bug work-around
  (require '[clojure.repl.deps :refer :all])
   ;; TODO consider strategy for running a server or not 
  (when-not remote
    (start-repl-server opts))
  (connect-to-prepl server port))) 

(comment
  
  ;; TODO cleanup all
  *default-data-reader-fn* 
  *data-readers*
  (start-repl-server {:port 5555 :server "localhost"}) 
  (def repl-conn (connect-to-prepl "localhost" 5555))
  
  (do-eval repl-conn '(+ 20 20) #(prn %))
  
  (pr-str *ns*)

  (pr-str '(in-ns 'repl-kit.core))
  (in-ns  'repl-kit.repl-eval)

  (pr-str 'repl-conn)

  *ns* 
  (ns-publics 'repl-kit.core)
  (ns-publics 'repl-kit.repl-eval)
  ;;
  )