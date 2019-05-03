(ns hello-world.datascript
  (:require
   [clojure.string :as s]
   [hello-world.names :refer [sample-names]]
   [datascript.db :as db]
   [datascript.core :as d]))


(def global-database
  (d/create-conn {:name {:db/index true}}))

(defonce some-dummy-data
 (d/transact! global-database (take 10 sample-names)))

;; Find the ID of user named "Bob"
(d/q '[:find ?e
        :where
       [?e :name "Bob"]]
      @global-database)

;; If you don't care about a field, you can leave it blank:
(d/q '[:find ?e
        :where
       [?e :name _]]
      @global-database)

;; If the blank is at the end of the where clause, it can be omitted:
(d/q '[:find ?e
        :where
       [?e :name]]
      @global-database)

(first (d/datoms @global-database :eavt))



;; Find the names of all females
(d/q '[:find ?name
       :where
       [?e :name ?name]
       [?e :sex "female"]]
     @global-database)

;; Take a subsample
(d/q '[:find (sample 3 ?e)
        :where
        [?e :name ?name]]
      @global-database)

;; Another operator...
(d/q '[:find (count ?e)
        :where
       [?e :name ?name]
       [?e :sex "female"]]
      @global-database)


;;;; The `pull' operator:
;;;;
;;
;; Returns query results as a map with the desired keys
(d/q '[:find (pull ?e [:name :sex])
       :where
          [?e :name ?name]
          [?e :name]]
     @global-database)




;;;; "Full" syntax

(d/q '[:find ?name
       :in $ ?gender
       :where
         [?e :name ?name]
         [?e :sex ?gender]]
     @global-database       ; <== This token gets bound to $ (the DB against which to run the query)
     "female")              ; <== This token gets bound to ?gender




;; Full syntax allows you to do cool things, like supply
;; predicates dynamically, e.g.

;; Give me the name of all MALES with a name starting with the letter "B" ?
(d/q '[:find ?name
       :in $            ; bound to the database
       ?gender          ; bound to "male"
       ?pred            ; bound to clojure.string/starts-with?
       ?starting-letter ; bound to "B"
       :where
         [?e :name ?name]
         [?e :sex ?gender]
         [(?pred ?name ?starting-letter)]]
     ;; The binding values follow:
     @global-database
     "male"
     clojure.string/starts-with?
     "B")


;;;; Rules  -- taming the verbosity

;; Suppose we are interested in young males:

(d/q '[:find ?name ?age
       :where
         [?e :name ?name]
         [?e :sex "male"]
         [?e :age ?age]
         [(< ?age 20)]]
     @global-database)

;; We can encapsulate this as a rule;
(def young-male-rule
  '[(is-young-male? ?e ?age)            ; This declares a new parseable expression
    [?e :age ?age]                      ; Followed by any number of normal where clause entries
    [?e :sex "male"]
    [(< ?age 20)]])

;; Rules are used like this:

(d/q '[:find (pull ?e [:name :sex :age])
       :in $ % ; <==  % (percent) is magic, like $, and means "the set of rules to parse".
       :where
       [?e :name ?name]
       (is-young-male? ?e)] ; <== uses new expression type.
     @global-database
     [young-male-rule]) ; <== List of Rules which supply new expressions supplied here



;;; If you have a bunch of rules which make sense in your domain,
;;; e.g. catching young drunk drivers...

(def riding-the-limit-rule '[(barely-legal? ?e)
                             [?e :age 19]])

(def pull-over-rule
  '[(pullover? ?e ?age)
    (is-young-male? ?e ?age)
    (barely-legal? ?e)])

(def nsw-police-ruleset
  [young-male-rule ; Must include ALL rules, recursively, that parser will need.
   riding-the-limit-rule
   pull-over-rule])

(d/q '[:find (pull ?e [:name :sex :age]) :in $ %
       :where
          [?e :name ?name]
          (pullover? ?e ?age)]
     @global-database
     nsw-police-ruleset)

;;; The END
