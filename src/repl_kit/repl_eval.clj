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

(defn dispatch-callback [f & args]
  (when (fn? f)
    (try
      (if (javax.swing.SwingUtilities/isEventDispatchThread)
        (apply f args)
        (javax.swing.SwingUtilities/invokeAndWait #(apply f args)))
      (catch Exception e
        (println (format "Exception during callback dispatch: %s" (.getMessage e)))))))

(defn write-output [tag text]
  (when (some? text)
    (let [value (str text)
          stream (if (= tag :err) System/err System/out)]
      (.print stream value)
      (.flush stream))))

(defn read-response
  ([connection]
   (read-response connection nil))
  ([connection on-output]
   (let [{:keys [reader]} connection]
     (loop []
       (when-let [line (.readLine reader)]
         (let [response (edn/read-string {:default tagged-literal} line)]
           (cond
             (and (map? response) (contains? #{:out :err} (:tag response)))
             (do
               (dispatch-callback on-output (:val response))
               (write-output (:tag response) (:val response))
               (recur))

             (and (map? response)
                  (contains? #{:ret :exception :root-ex} (:tag response)))
             response

             :else
             (recur))))))))

(defn when-done
  "Invoke a fn when a future completes. Returns a future wrapping the result
   of the fn to call."
  [future-to-watch fn-to-call]
  (future (fn-to-call @future-to-watch)))

(defn do-eval 
  "Evaluate a form in another thread. The provided function is 
   invoked when the evaluation completes. The function is passed
   the result of the evaluation."
  ([conn code f]
   (do-eval conn code f nil))
  ([conn code f on-output]
   {:pre [(fn? f)]}
   #_(println (format "code in eval is '%s'" code))
   (try 
     (when-done (future (send-form conn code)
                        (read-response conn on-output))
                (fn [result]
                  (dispatch-callback f result)))
     (catch Exception e
       (println (format "Exception during do-eval: %s" (.getMessage e)))))))

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