(ns ^:figwheel-hooks hello-world.app
  (:require
   [hello-world.names :refer [sample-names]]
   [hello-world.queries :as queries]
   [goog.dom :as gdom]
   [reagent.core :as reagent]
   [datascript.db :as db]
   [datascript.core :as core :refer [transact! q]]
   [clojure.string :as s]))

(enable-console-print!)


;;; Using a datascript in-memory db as the value Reagent reacts to:

(def global-database (core/create-conn {:name {:db/index true}}))

(def current-db (reagent/atom nil))

;; Heres the central trick --- reagent uses the famous ratom,
;; and we bind all changes to the global-database to update this ratom.

(core/listen! global-database
           (fn [x]
             (println "They just did a txn: got back " (count (:db-after x)) " datoms.")
             (reset! current-db (:db-after x))))


;;; Now we need functions to inspect and modify our database:

(defn add-entry! [name]
  (transact! global-database [[:db/add -1 :name name]
                              ;; Gender and age are just fake for now.
                              [:db/add -1 :sex "male"]
                              [:db/add -1 :age (rand-nth (range 10 89))]]))

(defn delete-entry! [id name]
  (transact! global-database [[:db/retract id :name name]]))

(defn build-pred [string]
  #(re-find (re-pattern (str "(?i)^\\s+$|" string)) %))

(defn all-names-matching
  "Returns a seq of  [name id] pairs for all
  entries in the DB matching the string S.
  Will return every name if S is blank."
  [s db]
  (println  "Matching for string " s)
  (queries/all-matching-persons-by-name db (build-pred s)))

(all-names-matching "aa" @current-db)

(add-entry! "Aaron")


;;;;  Page rendering

(defn val-of [node] (-> node .-target .-value))

(defn render-hits
  [hits]
  [:div.bg-info
   [:h1 "Filtered list (populated from @current-db)"]
   [:div.bg-primary
    [:ul
     (for [{:keys [:name :db/id]} (take 10 hits)]
       ^{:key (str "entity-" id)}
       [:li {:on-click #(delete-entry! id name)}
        [:strong name]])]]])

(defn selected-names-component []
  (let  [text (reagent/atom "")]
    (fn []
      [:div
       [:div.form-group
        [:label.col-sm-2  {:for "filter"} "Filter string:"]
        [:input.col-sm-4.col-push-6
         {:id "filter"
          :type :text
          :on-change #(reset! text (val-of %))}]
        [:h3.form-text.text-muted.pull-right
         "The database currently contains " [:mark (count @current-db)] " datoms."]]
       [render-hits (all-names-matching @text @current-db)]])))

(defn insert-data-component []
  [:div.form-group
   [:label.col-sm-2  {:for "new-name-field"} "Insert new names here"]
   [:input.col-sm-4.col-push-6
    {:id "new-name-field"
     :type :text
     :on-blur (fn [node]
                (add-entry! (val-of node))
                ;; Clear the text input.
                (set!  (-> node .-target .-value) ""))}]])

;;;; Boilerplate reagent stuff here...

(defn app-component []
  [:div.container
   [:div.jumbotron
    [:h1.text-center
     [:div.text-primary "Example Reagent"]
     [:div [:small  "and"]]
     [:div.text-primary "Datascript application"]]]
   [:form.form-horizontal
    [insert-data-component]
    [selected-names-component]]])

(defn mount [component el]
  (reagent/render-component component el))

(defn mount-app-element []
  (reagent/render-component [app-component] (gdom/getElement "app")))

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
