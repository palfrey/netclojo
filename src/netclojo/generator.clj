(ns netclojo.generator
	(:require
		[instaparse.core :as insta]
		[clojure.pprint :as pprint]
		[clojure.edn :as edn]
	)
	(:use [slingshot.slingshot :only [throw+ try+]])
)

(def netlogo
  (insta/parser (clojure.java.io/resource "netlogo.bnf"))
)

(defn parse [source]
	(let [simple (netlogo source)
		  multiple (insta/parses netlogo source)]
		(cond
			(insta/failure? simple)
				simple
			(= (count multiple) 1)
				(->>
					multiple
					first
					(insta/transform
						{
							:identifier keyword
							:number (comp edn/read-string str)
						}
					)
				)

			:default
				(throw+ {:type :ambiguous-parse :data multiple})
		)
	)
)

(defn detailed-parse [source]
	(try+
		(parse source)
		(catch [:type :ambiguous-parse] {:keys [:data]}
			(doall (map println data))
			(throw+)
		)
	)
)

(defn -main [file]
	(let [data (slurp file)]
		(println (detailed-parse data))
	)
)
