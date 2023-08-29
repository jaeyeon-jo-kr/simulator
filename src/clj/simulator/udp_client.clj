(ns simulator.udp-client
  (:require
   [simulator.config :as config]
   [clojure.data.codec.base64 :as b64])
  (:import java.net.InetAddress
           java.net.DatagramPacket
           java.net.DatagramSocket))

(defn message
  [text]
  (let [encoded (b64/encode (.getBytes text))]
    (DatagramPacket. encoded
                     (alength encoded)
                     (java.net.InetAddress/getByName "localhost")
                     (:port (:server config/config)))))

(defn trim-null-bytes
  [text]
  (clojure.string/replace text #"\x00" ""))

(defn log-message
  [text]
  (spit "client.log" (str text "\n") :append true))


(defn read-packet
  [^DatagramPacket packet]
  (-> (String. (b64/decode (.getData packet))
               java.nio.charset.StandardCharsets/UTF_8)
      trim-null-bytes))

(defn empty-message [n]
  (new DatagramPacket (byte-array n) n))

(defn send-message
  [socket]
  (let [content "hello!"]
    (log-message (str  "send : " content))
    (.send socket (message content))))

(defn receive-message
  [socket]
  (let [packet (empty-message 1024)]
    (.receive socket packet)
    (log-message (str "received : " (read-packet packet)))))

(comment
  (let [socket (DatagramSocket.)]
    (send-message socket)
    (receive-message socket))
  
  (InetAddress.)

  )