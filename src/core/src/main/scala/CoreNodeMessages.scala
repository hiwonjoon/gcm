/**
 * Created by wonjoon-g on 2014. 6. 4..
 */
package core
//case class TransformationJob(text: String)
//case class TransformationResult(text: String)
case class JobFailed(reason: String, job: Job)
case object BackendRegistration
