(ns panaeolus.libcsound64
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [cpath-clj.core :as cp])
  (:import [java.util.jar JarFile JarEntry]
           [java.io FileOutputStream]))

(set! *warn-on-reflection* true)

(defmacro while-let
  "The composition of a side-effects based while, and a single-binding let, ala if-let.\n\nAvoids loop/recur redundance."
  [[sym expr] & body]
  `(loop [~sym ~expr]
     (when ~sym
       ~@body
       (recur ~expr))))


(def ^:private current-csound-version "6.13")

(defn- get-os
  "Return the OS as a keyword. One of :windows :linux :mac"
  []
  (let [os (System/getProperty "os.name")]
    (cond
      (re-find #"[Ww]indows" os) :windows
      (re-find #"[Ll]inux" os)   :linux
      (re-find #"[Mm]ac" os)     :mac)))

(defn- get-cache-dir []
  (let [os (get-os)
        home (System/getProperty "user.home")]
    (case os
      :windows (let [local-app-data (or (System/getenv "APPDATA")
                                        (.getAbsolutePath
                                         (io/file home "AppData" "Local")))]
                 (.getAbsolutePath (io/file local-app-data "panaeolus" "Cache")))
      :linux (or (System/getenv "XDG_CACHE_HOME")
                 (.getAbsolutePath (io/file home ".cache" "panaeolus")))
      :mac (let [library (.getAbsolutePath (io/file home "Library"))]
             (.getAbsolutePath
              (io/file library "Caches" "panaeolus"))))))

(def ^:private ^java.io.File csound-cache-folder
  (io/file (get-cache-dir)
           (str "csound-" current-csound-version)))


(defn this-jar
  "utility function to get the name of jar in which this function is invoked"
  [& [^java.lang.Class ns]]
  (->>  ^java.lang.Class (or ns ^java.lang.Class (class *ns*))
        ^java.security.ProtectionDomain (.getProtectionDomain)
        ^java.security.CodeSource (.getCodeSource)
        ^java.net.URL (.getLocation)
        (.toURI)
        .getPath))

(defn cache-csound!
  "Cache csound and return the cache directory"
  []
  (let [os (get-os)
        classp-loc (io/file
                    "libcsound64"
                    (case os :linux "linux" :mac "darwin" :windows "windows")
                    "x86_64")
        resource-dir (cp/resources (.getPath classp-loc))
        cache-foler-location (.getAbsolutePath csound-cache-folder)]
    (when (and (= os :darwin) (not (.exists (io/file csound-cache-folder "Opcodes64"))))
      (.mkdirs (io/file csound-cache-folder "Opcodes64")))
    (when (and (= os :windows) (not (.exists (io/file csound-cache-folder "jack"))))
      (.mkdirs (io/file csound-cache-folder "jack")))
    (when (and (= os :windows) (not (.exists (io/file csound-cache-folder "win32libs"))))
      (.mkdirs (io/file csound-cache-folder "win32libs")))
    (if (empty? resource-dir)
      ;; FIXME: fix the pom.xml problem so this can be deleted
      (let [jar-file (JarFile. ^java.lang.String (this-jar))
            entries (enumeration-seq (.entries jar-file))]
        (doseq [^JarEntry entry entries]
		  (prn "ENTRY" entry)
          (let [entry-path (.getName entry)]
		    (prn entry-path)
            (when (and (string/includes? entry-path (.getPath classp-loc))
                       (not (.isDirectory entry)))
              (io/copy (.getInputStream jar-file entry)
                       (io/file cache-foler-location (.getName (io/file (.getName entry)))))))))
      (doseq [[file-name path-obj] resource-dir]
        (let [destination (io/file (str cache-foler-location file-name))]
          (when-not (.exists destination)
            (with-open [in (io/input-stream (first path-obj))]
              (io/copy in destination))))))
    cache-foler-location))

(defn spit-csound!
  "almost same as cache-csound, except it can choose any destination,
  returns nil"
  [dest]
  (doseq [os ["linux" "darwin" "windows"]]
    (let [classp-loc (io/file "libcsound64" os "x86_64")
          resource-dir (cp/resources (.getPath classp-loc))
          destination-dir (io/file dest classp-loc)]
      (.mkdirs (io/file destination-dir "Opcodes64"))
      (doseq [[file-name path-obj] resource-dir]
        (let [destination (io/file (str destination-dir file-name))]
          (with-open [in (io/input-stream (first path-obj))]
            (io/copy in destination)))))))
