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

(defn number-convert [& more]
  (if (or
	   (nil? (first more))
	   (= (count more) 0))
	[:item] (conj [:item] (apply edn/read-string more)))
)

(defn own-convert [named & more]
	(vec (concat [:own (keyword named)] more))
)

(defn line-convert [& more]
	(let [vecced (vec more)]
		(if (and (= (count vecced) 1) (vector? (first vecced))) (first vecced) vecced)
	)
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
							:string str
							:item number-convert
							:own own-convert
							:line line-convert
							:expressions line-convert
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
			(doall (map pprint/pprint data))
			(throw+)
		)
	)
)

(defn -main [file]
	(let [data (slurp file)]
		(println (detailed-parse data))
	)
)
