(ns repl-kit.repl-eval-test
  (:require [clojure.test :refer [deftest testing is]]
            [repl-kit.repl-eval :refer [read-response]]))

(deftest read-response-collects-until-final-ret
  (testing "read-response skips intermediate output frames and returns the final :ret"
    (let [reader (java.io.BufferedReader. (java.io.StringReader. (str "{:tag :out :val \"hello\\n\"}\n{:tag :ret :val 42 :ns user}\n")))
          connection {:reader reader}]
      (is (= {:tag :ret
              :val 42
              :ns 'user}
             (read-response connection))))))

(deftest read-response-forwards-output-callback
  (testing "read-response calls the output callback for :out and :err frames"
    (let [reader (java.io.BufferedReader. (java.io.StringReader. (str "{:tag :out :val \"hello\\n\"}\n{:tag :err :val \"oops\\n\"}\n{:tag :ret :val 42 :ns user}\n")))
          connection {:reader reader}
          events (atom [])]
      (is (= {:tag :ret
              :val 42
              :ns 'user}
             (read-response connection #(swap! events conj %))))
      (is (= ["hello\n" "oops\n"] @events)))))

(deftest read-response-writes-output-to-stdout
  (testing "read-response forwards :out/:err values to System/out and System/err"
    (let [reader (java.io.BufferedReader. (java.io.StringReader. (str "{:tag :out :val \"hello\\n\"}\n{:tag :ret :val 42 :ns user}\n")))
          connection {:reader reader}
          out-bytes (java.io.ByteArrayOutputStream.)
          old-out (System/out)
          old-err (System/err)]
      (try
        (System/setOut (java.io.PrintStream. out-bytes))
        (System/setErr (java.io.PrintStream. (java.io.ByteArrayOutputStream.)))
        (is (= {:tag :ret
                :val 42
                :ns 'user}
               (read-response connection)))
        (is (.contains (String. (.toByteArray out-bytes)) "hello\n"))
        (finally
          (System/setOut old-out)
          (System/setErr old-err))))))
