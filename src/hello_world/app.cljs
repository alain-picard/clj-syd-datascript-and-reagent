(ns ^:figwheel-hooks hello-world.app
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent]
   [datascript.db :as db]
   [datascript.core :as d]
   [clojure.string :as s]))

(enable-console-print!)

(def global-database (d/create-conn {:name { :db/index true }}))

(def current-db (reagent/atom nil))

(d/listen! global-database
           (fn [x]
             (println "They just did a txn: got back " (count (:db-after x)) " datoms.")
             (reset! current-db (:db-after x))))

(defn all-names-matching
  "Returns all names in the DB matching the string S.
   Will return every name if S is blank."
  [s db]
  (println  "Matching for string " s)
  (let [pred (if (s/blank? s)
               (constantly true)
               #(re-find (re-pattern (str "(?i)" s)) %))]
    (d/q '[:find ?name ?e
           :in $ ?pred
           :where
           [?e :name ?name]
           [(?pred ?name)]]
         db
         pred)))


;;;;  Page rendering

(defn render-hits [hits]
  [:div
   [:h4 "Filtered list"]
   [:ul
    (for [[x id] (take 10 hits)]
      ^{:key (str "entity-" id)}
      [:li {:on-click (fn [_]
                        (d/transact! global-database
                                     [[:db/retract id :name x]]))}
       x])]])

(defn sample-filtered-list []
  (let  [text (reagent/atom "")]
    (fn []
     [:div#ctrl
      [:h3.inline.text-info "Filter string:"]
      [:input {:type :text
               :on-change (fn [e] (reset! text (-> e .-target .-value)))}]
      [:span.h3 "    The database currently contains " [:span.h3.text-warning (count @current-db)] " datoms." ]
      [render-hits (all-names-matching @text @current-db)]])))

(defn insert-data-component []
  [:div#insert
   [:h3.inline.text-info "Insert new names here"]
   [:input {:type :text
            :on-blur (fn [e]
                       (d/transact! global-database
                                    [[:db/add -1 :name (-> e .-target .-value)]])
                       ;; Clear the text input.
                       (set! (-> e .-target .-value) nil))}]])

(defn hello-world []
  [:div#hello
   [:h1.text-primary "Example Reagent and Datascript application"]
   [insert-data-component]
   [sample-filtered-list]])

(defn mount [component el]
  (reagent/render-component component el))

(defn mount-app-element []
  (reagent/render-component [hello-world] (gdom/getElement "app")))

(defn initialize-database! []
 (d/transact! global-database
              [{:name "Ivan", :age 19 :sex "male"}
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
               {:name "Fred", :sex "male"}]))

(defn fill-database! [n]
  (d/transact! global-database
               (repeatedly n #(hash-map :name (str (cljs.core/random-uuid ))
                                        :sex (rand-nth ["male" "female"])))))


#_
(do (fill-database! 10000) 'ok)

;; specify reload hook with ^;after-load metadata
;; This is some weird figwheel trick

(defn ^:after-load on-reload []
  (mount-app-element)
  (initialize-database!))

(defonce start-up
  (do
    (initialize-database!)
    (mount-app-element)))


;;;; Testing/exploring forms below
;;;; Data script operations
#_
(d/transact! global-database
             [[:db/add -1 :name "Caleb"]
              [:db/add -1 :sex "Male"]])

#_
(all-names-matching "c" @global-database)

#_
(d/q '[:find ?e ?name
       :where
       [?e :name ?name]
       [?e :name]]
     @current-db)


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

#_
(d/reset-conn! global-database db1)


(defn find-by-sex [sex]
  (d/q `[:find ?name
         :where
         [?e :name ?name]
         [?e :sex ~sex]]
       db1))

(find-by-sex "male")


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



:app
