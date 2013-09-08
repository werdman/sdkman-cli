package gvm

class GvmBashEnvBuilder {

    final buildScriptDir = "build/testScripts" as File

    //mandatory fields
    private final File gvmBase
    private final CurlStub curlStub

    //optional fields with sensible defaults
    List candidates = ['groovy', 'grails']
    List availableCandidates = candidates
    boolean onlineMode = true
    boolean forcedOfflineMode = false
    String broadcast = "This is a LIVE broadcast!"
    String service = "http://localhost:8080"
    String jdkHome = "/path/to/my/jdk"

    File gvmDir, gvmBinDir, gvmVarDir, gvmSrcDir

    BashEnv bashEnv

    static GvmBashEnvBuilder create(File gvmBase, CurlStub curlStub){
        new GvmBashEnvBuilder(gvmBase, curlStub)
    }

    private GvmBashEnvBuilder(File gvmBase, CurlStub curlStub){
        this.gvmBase = gvmBase
        this.curlStub = curlStub
    }

    GvmBashEnvBuilder withCandidates(List candidates){
        this.candidates = candidates
        this
    }

    GvmBashEnvBuilder withAvailableCandidates(List candidates){
        this.availableCandidates = candidates
        this
    }

    GvmBashEnvBuilder withBroadcast(String broadcast){
        this.broadcast = broadcast
        this
    }

    GvmBashEnvBuilder withOnlineMode(boolean onlineMode){
        this.onlineMode = this.onlineMode
        this
    }

    GvmBashEnvBuilder withForcedOfflineMode(boolean forcedOfflineMode){
        this.forcedOfflineMode = forcedOfflineMode
        this
    }

    GvmBashEnvBuilder withService(String service){
        this.service = service
        this
    }

    GvmBashEnvBuilder withJdkHome(String jdkHome){
        this.jdkHome = jdkHome
        this
    }

    BashEnv build() {
        gvmDir = prepareDirectory(gvmBase, ".gvm")
        gvmBinDir = prepareDirectory(gvmDir, "bin")
        gvmVarDir = prepareDirectory(gvmDir, "var")
        gvmSrcDir = prepareDirectory(gvmDir, "src")

        initializeCandidates(gvmDir, candidates)
        initializeAvailableCandidates(gvmVarDir, availableCandidates)
        initializeBroadcast(gvmVarDir, broadcast)

        primeInitScript(gvmBinDir)
        primeModuleScripts(gvmSrcDir)

        bashEnv = new BashEnv(gvmBase.absolutePath, [
            GVM_DIR: gvmDir.absolutePath,
            GVM_ONLINE: onlineMode,
            GVM_FORCE_OFFLINE: forcedOfflineMode,
            GVM_SERVICE: service,
            JAVA_HOME: jdkHome
        ])
    }

    private prepareDirectory(File target, String directoryName) {
        def directory = new File(target, directoryName)
        directory.mkdirs()
        directory
    }

    private initializeCandidates(File folder, List candidates) {
        candidates.each { candidate ->
            new File(folder, candidate).mkdirs()
        }
    }

    private initializeAvailableCandidates(File folder, List candidates){
        new File(folder, "candidates") << candidates.join(",")
    }

    private initializeBroadcast(File targetFolder, String broadcast) {
        new File(targetFolder, "broadcast") << broadcast
    }

    private primeInitScript(File targetFolder) {
        def sourceInitScript = new File(buildScriptDir, 'gvm-init.sh')

        if (!sourceInitScript.exists())
            throw new IllegalStateException("gvm-init.sh has not been prepared for consumption.")

        def destInitScript = new File(targetFolder, "gvm-init.sh")
        destInitScript << sourceInitScript.text
        destInitScript
    }

    private primeModuleScripts(File targetFolder){
        for (f in buildScriptDir.listFiles()){
            if(!(f.name in ['selfupdate.sh', 'install.sh', 'gvm-init.sh'])){
                new File(targetFolder, f.name) << f.text
            }
        }
    }

}