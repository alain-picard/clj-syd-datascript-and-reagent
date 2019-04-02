
(ns hello-world.image
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))


(defn draw []
  (q/background 255)
  ; create image and draw gradient on it
  (let [im (q/create-image 100 100 :rgb)]
    (dotimes [x 100]
      (dotimes [y 100] (q/set-pixel im x y (q/color (* 2 x) (* 2 y) (+ x y)))))
    ; draw image twice
    (q/image im 0 0)
    (q/image im 50 50)))

(defn load-sketch []
  (q/defsketch my
    :host "host"
    :size [500 500]
    :draw draw))

;;;;;
(defn setup []
  ; Set frame rate to 30 frames per second.
  (q/frame-rate 60)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/background 255 0 0)
  (q/color-mode :hsb)
  ; setup function returns initial state. It contains
  ; circle color and position.
  {:color 0
   :angle 0})

(defn update-state [state]
  ; (throw "Oops")
  (-> state
      (update :color #(mod (+ % 0.2) 255))
      (update :angle + 0.1)))

(def drawing? (atom true))

(defn toggle-animation []
  (swap! drawing? not))

(defn handle-keystroke [k]
  (println "Handling key " k)
  (case k
    :s (reset! drawing? false)
    :g (reset! drawing? true)
    (toggle-animation)))

(def last-keypress? (atom nil))

(defn draw-state [state]
  (when (q/key-pressed?)
    (when-not (= @last-keypress? (js/String (q/raw-key)))
      (println "They hit " (q/raw-key) (q/key-as-keyword))
      (handle-keystroke (q/key-as-keyword)))
    (reset! last-keypress? (js/String (q/raw-key))))

  (when @drawing?
    (when (q/mouse-pressed?)
      (q/background (rand 255) (rand 255) (rand 255)))

    ;; Set circle color.
    (q/fill (:color state) 255 255)
    ;; Calculate x and y coordinates of the circle.
    (let [w (/ (q/width) 2)
          h (/ (q/height) 2)
          angle (:angle state)
          A 1.1
          B .8
          delta 90
          x (* w (q/sin (+ delta (* A angle))))
          y (* h (q/cos (* B angle)))
          x (/ x Math/PI)
          y (/ y Math/PI)
          ]
      ;; Move origin point to the center of the sketch.
      (q/with-translation [w h]
        (q/with-rotation [(* (/ -3.14159 4 -2) angle)]
          (q/ellipse x y
                     100
                     50))))))

; this function is called in index.html
(defn run-sketch []
  (q/defsketch my-sketch
    :host "host"
;    :size [512 512]
    :size [1920 1200]
    :setup setup                    ; setup function called only once, during sketch initialization.
    :update update-state            ; update-state is called on each iteration before draw-state.
    :draw draw-state
    :middleware [m/fun-mode]
    :features [:present]))

; uncomment this line to reset the sketch:
; (run-sketch)

#_
(js/alert "Hello, clj-syd!")

#_
(let
  [sh (q/load-shape
        "https://upload.wikimedia.org/wikipedia/en/2/22/Heckert_GNU_white.svg")]
  (q/shape sh 0 0 500 500))
