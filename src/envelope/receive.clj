(ns envelope.receive
  (:import [java.util
            ArrayList
            Date
            Properties
            Base64]
           [jakarta.mail
            Message
            Session
            Authenticator
            PasswordAuthentication
            Transport
            Message$RecipientType]
           [jakarta.mail.internet
            MimeMessage
            MimeBodyPart
            InternetAddress
            MimeMultipart]
           [java.io
            FileOutputStream
            File]
           [jakarta.activation
            DataHandler]
           [java.io ByteArrayInputStream])
  (:gen-class))


(defn parse-email [raw-email]
  (let [session (Session/getDefaultInstance (java.util.Properties.))
        input-stream (ByteArrayInputStream. (.getBytes raw-email))
        message (MimeMessage. session input-stream)]
    {:message-id (.getMessageID message)
     :in-reply-to (.getHeader message "In-Reply-To")
     :references (.getHeader message "References")
     :original-recipient (.getHeader message "Original-Recipient")}))


;; Example usage:
(comment
(def raw-email-data (slurp "file-location.txt"))
(def parsed-email (parse-email raw-email-data))
(:message-id parsed-email)
(:references parsed-email)
)
