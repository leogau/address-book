(ns address-book.core.address-book-tests
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [address-book.core.handler :refer :all]
            [address-book.core.models.query-defs :as query]))

(facts "Example GET and POST tests"
  (with-state-changes [(before :facts (query/create-contacts-table-if-not-exists!))
                       (after :facts (query/drop-contacts-table!))]
    (fact "Test GET"
      (query/insert-contact<! {:name "JT" :phone "(123)" :email "jt@jt.com"})
      (query/insert-contact<! {:name "Ut" :phone "(456)" :email "ut@fd.com"})
      (let [response (app (mock/request :get "/"))]
        (:status response) => 200
        (:body response) => (contains "JT")
        (:body response) => (contains "Ut")))

    (fact "Test POST"
      (count (query/all-contacts)) => 0
      (let [response (app (mock/request :post "/post" {:name "some" :phone "(345)" :email "a@a.com"}))]
        (:status response) => 302
        (count (query/all-contacts)) => 1))

    (fact "Test UPDATE a contact"
      (query/insert-contact<! {:name "leo" :phone "(321)" :email "leo@leo"})
      (let [response (app (mock/request :phone "/edit/1" {:id "1" :name "leo2" :phone "(34)" :email "boom"}))]
        (:status response) => 302
        (count (query/all-contacts)) => 1
        (first (query/all-contacts)) => {:id "1" :name "leo2" :phone "(34)" :email "boom"}))

    (fact "Test DELETE"
      (query/insert-contact<! {:name "leo" :phone "(123)" :email "boom"})
      (count (query/all-contacts)) => 1
      (let [response (app (mock/request :post "/delete/1" {:id 1}))]
        (count (query/all-contacts)) => 0))))
