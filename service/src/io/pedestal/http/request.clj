; Copyright 2014-2016 Cognitect, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns io.pedestal.http.request
  (:import (javax.servlet.http HttpServletRequest)))

(defprotocol ProxyDatastructure
  (realized [this] "Return fully-realized version of underlying data structure."))

(extend-protocol ProxyDatastructure
  nil
  (realized [t] nil))

(defn servlet-request-headers [^HttpServletRequest servlet-req]
  (loop [out (transient {})
         names (enumeration-seq (.getHeaderNames servlet-req))]
    (if (seq names)
      (let [^String key (first names)
            hdrstr (java.lang.String/join "," ^clojure.lang.EnumerationSeq (enumeration-seq (.getHeaders servlet-req key)))]
        (recur (assoc! out (.toLowerCase key) hdrstr)
               (rest names)))
      (persistent! out))))

(defn servlet-path-info [^HttpServletRequest request]
  (let [path-info (.substring (.getRequestURI request)
                              (.length (.getContextPath request)))]
    (if (.isEmpty path-info)
      "/"
      path-info)))

(defprotocol ContainerRequest
  (server-port [x])
  (server-name [x])
  (remote-addr [x])
  (uri [x])
  (query-string [x])
  (scheme [x])
  (request-method [x])
  (protocol [x])
  (headers [x])
  (header [x header-string])
  (ssl-client-cert [x])
  (body [x])
  (path-info [x])
  (async-supported? [x])
  (async-started? [x]))

(extend-protocol ContainerRequest
  nil
  (server-port [x] nil)
  (server-name [x] nil)
  (remote-addr [x] nil)
  (uri [x] nil)
  (query-string [x] nil)
  (scheme [x] nil)
  (request-method [x] nil)
  (protocol [x] nil)
  (headers [x] nil)
  (header [x header-string] nil)
  (ssl-client-cert [x] nil)
  (body [x] nil)
  (path-info [x] nil)
  (async-supported? [x] false)
  (async-started? [x] false)

  HttpServletRequest
  (server-port [req] (.getServerPort req))
  (server-name [req] (.getServerName req))
  (remote-addr [req] (.getRemoteAddr req))
  (uri [req] (.getRequestURI req))
  (query-string [req] (.getQueryString req))
  (scheme [req] (keyword (.getScheme req)))
  (request-method [req] (keyword (.toLowerCase (.getMethod req))))
  (protocol [req] (.getProtocol req))
  (headers [req] (servlet-request-headers req))
  (header [req header-string] (.getHeader req header-string))
  (ssl-client-cert [req] (.getAttribute req "javax.servlet.request.X509Certificate"))
  (body [req] (.getInputStream req))
  (path-info [req] (servlet-path-info req))
  (async-supported? [req] (.isAsyncSupported req))
  (async-started? [req] (.isAsyncStarted req)))

(def ring-dispatch
  {:server-port server-port
   :server-name server-name
   :remote-addr remote-addr
   :uri uri
   :query-string query-string
   :scheme scheme
   :request-method request-method
   :headers headers
   :ssl-client-cert ssl-client-cert
   :body body
   :path-info path-info
   :protocol protocol
   :async-supported? async-supported?})

(def nil-fn (constantly nil))

