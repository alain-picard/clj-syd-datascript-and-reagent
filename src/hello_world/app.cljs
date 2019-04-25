(ns ^:figwheel-hooks hello-world.app
  (:require
   [hello-world.names :refer [sample-names]]
   [goog.dom :as gdom]
   [reagent.core :as reagent]
   [datascript.db :as db]
   [datascript.core :as core :refer [transact! q]]
   [clojure.string :as s]))

(enable-console-print!)

(def global-database (core/create-conn {:name {:db/index true}}))

;; Heres the central trick --- reagent uses the famous ratom,
;; and we bind all changes to the global-database to update this ratom.

(def current-db (reagent/atom nil))

(core/listen! global-database
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
    (q '[:find ?name ?e
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
                        (transact! global-database
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
                       (core/transact! global-database
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
 (core/transact! global-database sample-names))

(defn fill-database! [n]
   (core/transact! global-database
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

:app
