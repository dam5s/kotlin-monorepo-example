import com.google.protobuf.gradle.ProtobufExtension
import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.id
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply<ProtobufPlugin>()

    val libs = rootProject.libs

    configure<KotlinJvmProjectExtension> {
        dependencies {
            "implementation"(libs.bundles.grpc)
        }
    }

    configure<ProtobufExtension> {
        val usingWindowsArm64 = Os.isFamily(Os.FAMILY_WINDOWS) && Os.isArch("aarch64")

        protoc {
            artifact = libs.protobuf.protoc.get().toString()

            if (usingWindowsArm64) {
                artifact = "$artifact:windows-x86_64"
            }
        }

        plugins {
            id("grpc-java") {
                artifact = libs.grpc.java.gen.get().toString()

                if (usingWindowsArm64) {
                    artifact = "$artifact:windows-x86_64"
                }
            }

            id("grpc-kotlin") {
                val kotlinGenArtifact = libs.grpc.kotlin.gen.get().toString()

                // see https://github.com/grpc/grpc-kotlin/issues/273 for :jdk8@jar
                artifact = "$kotlinGenArtifact:jdk8@jar"
            }
        }

        generateProtoTasks {
            ofSourceSet("main").forEach {
                it.plugins {
                    id("grpc-java") {}
                    id("grpc-kotlin") {}
                }
            }
        }
    }
}
