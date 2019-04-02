(ns ^:figwheel-hooks hello-world.app
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [datascript.db :as db]
   [datascript.core :as d]
   [clojure.string :as s]))



(def db1
  (-> (d/empty-db)
      (d/db-with (shuffle [{:name "Ivan", :age 19 :sex "male"}
                           {:name "Katerina", :sex "female"}
                           {:name "Christine", :sex "female"}
                           {:name "Bob", :sex "male"}
                           {:name "Helen", :sex "female"}
                           {:name "Susan", :sex "female"}
                           {:name "Samantha", :sex "female"}
                           {:name "Chlöe", :sex "female"}
                           {:name "Jack", :sex "male"}
                           {:name "John", :sex "male"}
                           {:name "James", :sex "male"}
                           {:name "Leo", :sex "male"}
                           {:name "Fred", :sex "male"}]))))

(d/q '[:find ?e ?name
       :where
       [?e :name ?name]
       [?e :name]]
     db1)


(defn find-by-sex [sex]
  (d/q `[:find ?name
         :where
         [?e :name ?name]
         [?e :sex ~sex]]
       db1))

(find-by-sex "male")

(defn all-names-matching
  "Returns all names in the DB matching the string S.
   Will return every name if S is blank."
  [s]
  (let [pred (if (s/blank? s)
               (constantly true)
               #(re-find (re-pattern (str "(?i)" s)) %))]
    (d/q '[:find ?name
           :in $ ?pred
           :where
           [?e :name ?name]
           [(?pred ?name)]]
         db1
         pred)))




(defn get-app-element []
  (gdom/getElement "app"))

(defn render-hits [hits]
  [:div
   [:h4 "filtered list"]
   [:ul
    (for [[x] hits]
      [:li x])]])

(let [text (atom "")]
  (defn sample-control []
    [:div#ctrl
     [:h3 "Enter string:"]
     [:input {:type :text
              :on-change (fn [e] (reset! text (-> e .-target .-value)))}]
     (render-hits (all-names-matching @text))]))


(defn hello-world []
  [:div#hello
   (sample-control)])

(defn mount [component el]
  (reagent/render-component component el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount [hello-world] el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element))



;;;; Testing/exploring forms below
;;;; Data script operations


(def sample-entities
  [{:db/id 1 :name "Ivan" :age 10}
   {:db/id 2 :name "Ivan" :age 20}
   {:db/id 3 :name "Oleg" :age 10}
   {:db/id 4 :name "Oleg" :age 20}])

(def db (d/db-with (d/empty-db) sample-entities))

(d/q '[:find  ?e ?a ?n
       :where [?e :age ?a]
       [?e :name ?n]] db)

(d/q '[:find  ?e ?a ?n
       :where
       [?e :age ?a]
       [?e :name ?n]
       [(= ?n "Ivan")]] db)

(d/q '[:find  ?e ?a ?n
       :where
       [?e :age ?a]
       [?e :name ?n]
       [(re-find #"(?i)iv" ?n)]]
     db)


(-> (d/empty-db)
      (d/db-with [{ :name "Ivan", :age 19 :sex "male"}
                  { :name "Katerina", :sex "female"}]))

(let [db (d/empty-db)]
  (d/init-db
              '[[sally :age 21]
                 [fred :age 42]
                 [ethel :age 42]
                 [fred :likes pizza]
                 [sally :likes opera]
                 [ethel :likes sushi]]))

:app
