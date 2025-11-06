(ns repl-kit.form-parse-test
  (:require [clojure.test :refer [use-fixtures testing run-tests is are deftest]]
            [repl-kit.form-parse :refer [form-txt]]))
           
;; TODO - this test is a little hard to read.
;; Consider another structure for this
(deftest form-parse-test
  (testing "Parse Form At Current Cursor Position"
    (is (= (form-txt "*ns*" 4)
           "*ns*"))
    (is (= (form-txt "{:a 1}" 6)
           "{:a 1}"))
    (is (= (form-txt "  #[0 1 2]" 10)
           "[0 1 2]"))
    (is (= (form-txt "(+ 1" 4)
           " 1"))
    (is (= (form-txt "(+" 2)
           "(+"))
    (is (= (form-txt "(+ " 2)
           "(+"))
    (is (= (form-txt "(" 1)
           "("))))

(comment 
  (run-tests)

  ;;
  )