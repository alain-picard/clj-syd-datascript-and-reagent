(defproject hello-world.app "0.1.0-SNAPSHOT"
  :description "Example of clojurescript, reagent and QUIL."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.7.1"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [ring "1.7.1"]
                 [http-kit "2.3.0"]
                 [reagent "0.8.1"]
                 [datascript "0.18.2"]
                 [quil "2.8.0"]]

  :source-paths ["src"]
  :main hello-world.server

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" hello-world.test-runner]}

  :profiles {:dev {:dependencies [[com.bhauman/figwheel-main "0.2.1-SNAPSHOT"]
                                  [devcards "0.2.6"]
                                  #_ ; We don't need this one.  We have a real REPL, i.e. emacs.  :-)
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]]}})
