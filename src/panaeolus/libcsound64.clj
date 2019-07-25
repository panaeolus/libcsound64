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
      (re-find #"[Mm]ac" os)     :darwin)))

(defn- get-cache-dir []
  (let [os (get-os)
        home (System/getProperty "user.home")]
    (case os
      :windows (let [local-app-data (or (System/getenv "LOCALAPPDATA")
                                        (.getAbsolutePath
                                         (io/file home "AppData" "Roaming")))]
                 (.getAbsolutePath (io/file local-app-data "panaeolus" "Cache")))
      :linux (or (System/getenv "XDG_CACHE_HOME")
                 (.getAbsolutePath (io/file home ".cache" "panaeolus")))
      :darwin (let [library (.getAbsolutePath (io/file home "Library"))]
                (.getAbsolutePath
                 (io/file library "Caches" "panaeolus"))))))

(defn csound-cache-folder []
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

(defn ensure-unix-path [^String path]
  (-> path
      (string/replace "\\\\" "/")
      (string/replace "\\" "/")))

(defn cache-csound!
  "Cache csound and return the cache directory"
  []
  (let [os (get-os)
        classp-loc (io/file "libcsound64" (name os) "x86_64")
        resource-dir (cp/resources (.getPath classp-loc))
        cache-folder (csound-cache-folder)
        cache-foler-location (.getAbsolutePath ^java.io.File cache-folder)]
    (when (and (= os :darwin) (not (.exists (io/file cache-folder "Opcodes64"))))
      (.mkdirs (io/file cache-folder "Opcodes64")))
    (when (and (= os :linux) (not (.exists (io/file cache-folder "csound"))))
      (.mkdirs (io/file cache-folder "csound"))
      (when (not (.exists (io/file cache-folder "csound" "plugins64-6.0")))
        (.mkdirs (io/file cache-folder "csound" "plugins64-6.0"))))
    (when (and (= os :windows) (not (.exists (io/file cache-folder "jack"))))
      (.mkdirs (io/file cache-folder "jack")))
    (when (and (= os :windows) (not (.exists (io/file cache-folder "win32libs"))))
      (.mkdirs (io/file cache-folder "win32libs")))
    (if (empty? resource-dir)
      (let [jar-file (JarFile. ^java.lang.String (this-jar))
            entries (enumeration-seq (.entries jar-file))]
        (doseq [^JarEntry entry entries]
          (let [entry-path (.getName entry)]
            (when (and (string/includes? entry-path (ensure-unix-path (.getPath classp-loc)))
                       (not (.isDirectory entry)))
              (let [relative-path (-> entry-path
                                      (string/replace (ensure-unix-path (.getPath classp-loc)) "")
                                      (string/replace #"^/" ""))
                    destination (io/file cache-foler-location relative-path)]
                (when-not (.exists destination)
                  (io/copy (.getInputStream jar-file entry) destination)))))))
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
      (.mkdirs destination-dir)
      (when (and (= os "darwin") (not (.exists (io/file destination-dir "Opcodes64"))))
        (.mkdirs (io/file destination-dir "Opcodes64")))
      (when (and (= os "linux") (not (.exists (io/file destination-dir "csound"))))
        (.mkdirs (io/file destination-dir "csound"))
        (when (not (.exists (io/file destination-dir "csound" "plugins64-6.0")))
          (.mkdirs (io/file destination-dir "csound" "plugins64-6.0"))))
      (when (and (= os "windows") (not (.exists (io/file destination-dir "jack"))))
        (.mkdirs (io/file destination-dir "jack")))
      (when (and (= os "windows") (not (.exists (io/file destination-dir "win32libs"))))
        (.mkdirs (io/file destination-dir "win32libs")))
      (doseq [[file-name path-obj] resource-dir]
        (let [destination (io/file (str destination-dir file-name))]
          (with-open [in (io/input-stream (first path-obj))]
            (io/copy in destination)))))))
