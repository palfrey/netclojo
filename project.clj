(defproject netclojo "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/clojurescript "0.0-2227"]
                 [domina "1.0.2-SNAPSHOT"]
                 ]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src"]

  :profiles {:generator
			 {
			  :dependencies
				[
					[instaparse "1.3.3"]
					[midje "1.6.3"]
					[slingshot "0.10.3"]
				]
			  :main "netclojo.generator"
			  :plugins [[lein-midje "3.0.0"]]
			  :source-paths ["src" "resources"]
			 }
			}
  :aliases {"generator" ["with-profile" "+generator" "run"]}

  :cljsbuild {
    :builds [{:id "netclojo"
              :source-paths ["src"]
              :compiler {
                :output-to "netclojo.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
