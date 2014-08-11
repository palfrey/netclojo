(ns netclojo.generator
  (:require [instaparse.core :as insta]))

(def netlogo
  (insta/parser (clojure.java.io/resource "netlogo.bnf"))
)

(defn -main [file]
	(let [data (slurp file)
		  simple (netlogo data)]
		(if (insta/failure? simple)
			(println simple)
			(
				(println "Ambiguous parse!")
				(doall (map println (insta/parses netlogo data)))
				(System/exit -1)
			)
		)
	)
)
