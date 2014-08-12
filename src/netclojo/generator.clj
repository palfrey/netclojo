(ns netclojo.generator
  (:require
   [instaparse.core :as insta]
   [clojure.pprint :as pprint]
   [clojure.edn :as edn]))

(def netlogo
  (insta/parser (clojure.java.io/resource "netlogo.bnf"))
)

(defn -main [file]
	(let [data (slurp file)
		  simple (netlogo data)
		  multiple (insta/parses netlogo data)]
		(cond
			(insta/failure? simple)
				(println simple)
			(= (count multiple) 1)
				(pprint/pprint (->> multiple first
								(insta/transform
									{
									 :identifier keyword
									 :number (comp edn/read-string str)
									}
								)
							)
				)
			:default
				(
					(println "Ambiguous parse!")
					(doall (map println (insta/parses netlogo data)))
					(System/exit -1)
				)
		)
	)
)
