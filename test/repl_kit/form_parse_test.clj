(ns repl-kit.form-parse-test
  (:require [clojure.test :refer [use-fixtures testing run-tests is are deftest]]
            [repl-kit.form-parse :refer [form-txt]]))
           
(deftest form-parse-test
  (testing "Parse Form At Current Cursor Position"
    (is (= (form-txt "*ns*" 4)
           "*ns*"))
    (is (= (form-txt "{:a 1}" 6)
           "{:a 1}"))
    (is (= (form-txt "  #[0 1 2]" 10)
           "[0 1 2]"))))

(comment 
  (run-tests)
  ;;
  )