(ns simulator.udp-server
  (:require [simulator.config :as config]
            [clojure.core.async :as a]
            [clojure.data.codec.base64 :as b64]
            [simulator.log :as log])
  (:import java.net.DatagramPacket
           java.net.DatagramSocket))

(def port 
  (:port (:server config/config)))

(defonce server (atom nil))

(defonce client-id-list (atom {}))

(defn publish-client-id
  []
  (let [id (java.util.UUID/randomUUID)]
    (swap! client-id-list conj [id {}])
    id))

(comment 
  client-id-list
  (publish-client-id)
  (String. 
   (b64/decode
    (b64/encode (.getBytes "abc")))))

(defn trim-null-bytes
  [text]
  (clojure.string/replace text #"\x00" ""))

(defn empty-message [n]
  (new DatagramPacket (byte-array n) n))

(defn crop-text
  [text]
  (re-matches #".+" text))

(defn read-packet
  [^DatagramPacket packet]
  (-> (String. (b64/decode (.getData packet))
               java.nio.charset.StandardCharsets/UTF_8)
      (trim-null-bytes)))

(defn log-message
  [text]
  (spit "server.log" (str text "\n") :append true))

(defn receive-message
  []
  (let [message (empty-message 1024)]
    (.receive @server message)
    (log-message (str "received : " (read-packet message)))
    message))

(defn write-packet
  [text address port]
  (doto (empty-message 1024)
    (.setData (b64/encode (.getBytes text)))
    (.setAddress address)
    (.setPort port)))


(defn send-response
  [^DatagramPacket received-packet]
  (let [response (str "Published id : " (publish-client-id))]
    (log-message (str "sended : " response))
    (.send @server
           (write-packet response
                         (.getAddress received-packet)
                         (.getPort received-packet)))))

(defn handle-message
  []
  (let [packet (receive-message)]
    (send-response packet)))

(defn start-loop
  []
  (log-message "Start server.")
  (a/go-loop [s @server]
    (when s
      (handle-message)
      (Thread/sleep 100)
      (recur @server))))


(defn start!
  []
  (reset! server (DatagramSocket. port)) 
  (start-loop))

(defn stop!
  []
  (when @server
    (.close @server)
    (reset! server nil)
    (log-message "Server closed.")))


(comment 
  (start!)

  (stop!)
  )


