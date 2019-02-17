(ns ^:figwheel-hooks hello-world.app
  (:require
   [goog.dom :as gdom]
   [hello-world.image :as image]
   [reagent.core :as reagent :refer [atom]]))


(println "This text is printed from src/hello_world/app.cljs. Go ahead and edit it and see reloading in action.")

(defn multiply [a b] (* a b))


;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hello world!"
                          :counter 0}))

(def colors [:red :green :blue])

(defn inc-counter []
  (println "Incrementing " @app-state)
  (swap! app-state
         update
         :counter
         inc))

(defn get-app-element []
  (gdom/getElement "app"))

(defn hello-world []
  [:div
   [:h1 {:on-click inc-counter}  (:text @app-state) " and counter is "
    [:span {:style {:color (nth colors (mod (:counter @app-state) 3) )}} (:counter @app-state)]]
   [:h3 "Edit this in src/hello_world/app.cljs and watch it change!"]
   [:h3 "OK, then, here we go"]])

(defn sketch []
  [:div
   [:input
    {:type "button"
     :value "Click me!"
     :on-click #'image/toggle-animation}]
   [:canvas#host]])

(defn mount [component el]
  (reagent/render-component component el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount [hello-world] el)
    (mount [sketch] (gdom/getElement "sketch"))
    (image/run-sketch)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  (println "Reloaded")
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

#_
(reset! app-state {:text "Goodbye world!", :counter 15})

(println "foobar")
