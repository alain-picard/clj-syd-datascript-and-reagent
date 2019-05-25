(ns hello-world.queries
  (:require [datascript.core :as d]))


;;;;;; GENERATED : DO NOT EDIT!! ;;;;;

(def person-pull [:age :name :sex])
(def person-query '[:find (pull ?e []) :where])
(defn find-persons [db] (->> db (d/q person-query) (map first)))
(defn
  all-matching-persons-by-name
  [db pred]
  (->>
   pred
   (d/q
    '[:find
      (pull ?e [:db/id :age :name :sex])
      :in
      $
      ?pred
      :where
      [?e :age ?age]
      [?e :name ?name]
      [?e :sex ?sex]
      [(?pred ?name)]]
    db)
   (map first)
   (sort-by :name)))
(defn
  all-matching-persons-by-sex
  [db pred]
  (->>
   pred
   (d/q
    '[:find
      (pull ?e [:db/id :age :name :sex])
      :in
      $
      ?pred
      :where
      [?e :age ?age]
      [?e :name ?name]
      [?e :sex ?sex]
      [(?pred ?sex)]]
    db)
   (map first)
   (sort-by :sex)))
(defn
  all-matching-persons-by-age
  [db pred]
  (->>
   pred
   (d/q
    '[:find
      (pull ?e [:db/id :age :name :sex])
      :in
      $
      ?pred
      :where
      [?e :age ?age]
      [?e :name ?name]
      [?e :sex ?sex]
      [(?pred ?age)]]
    db)
   (map first)
   (sort-by :age)))
