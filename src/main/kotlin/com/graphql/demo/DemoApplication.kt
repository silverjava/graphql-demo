package com.graphql.demo

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET

@SpringBootApplication
@EnableFeignClients
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}

data class Todo(val userId: Int, val id: Int, val title: String, val completed: Boolean)
data class Post(val id: Int, val userId: Int, val title: String, val body: String)
data class Comment(val id: Int, val postId: Int, val name: String, val email: String, val body: String)
data class Tech(val name: String, val date: String)
data class Assignment(val name: String, val date: String, val job: String)
data class Kpi(val key: String, val value: Float)
data class Job(val name: String)

@Component
class QueryResolver: GraphQLQueryResolver {
	@Autowired
	private lateinit var todoRepository: TodoRepository

	@Autowired
	private lateinit var postRepository: PostRepository

	fun todos() = todoRepository.todos()
	fun todo(id: Int) = todoRepository.findTodoById(id)

	fun posts() = postRepository.posts()
	fun post(id: Int) = postRepository.getPostById(id)

	fun dashboard(name: String, date: String) = Tech(name, date)
}

@Component
class PostResolver: GraphQLResolver<Post> {
	@Autowired
	private lateinit var postRepository: PostRepository

	fun comments(post: Post) = postRepository.getCommentsByPostId(post.id)
	fun sum(post: Post) = post.id + post.userId
}

//---------------------------------------------------------------
//---------------------------------------------------------------

@Component
class DashboardResolver: GraphQLResolver<Tech> {
	fun kpi(tech: Tech): Array<Kpi> {
		println("Hello, Itd's KPi")
		return arrayOf(Kpi("hello", 1.2f))
	}
	fun nextAssignments(tech: Tech) = arrayOf(Assignment(tech.name, tech.date, "hello world"))
}

@Component
class AssignmentResolver: GraphQLResolver<Assignment> {
	fun job(assignment: Assignment) = Job("Dumb job")
}

@FeignClient("todos")
interface TodoRepository {
	@RequestMapping(value = ["/todos"], method = [GET])
	fun todos(): Array<Todo>

	@RequestMapping(value=["/todos/{id}"], method = [GET])
	fun findTodoById(@PathVariable("id") id: Int): Todo
}

@FeignClient("posts")
interface PostRepository {
	@RequestMapping(value = ["/posts"], method = [GET])
	fun posts() : Array<Post>

	@RequestMapping(value=["/posts/{id}"], method = [GET])
	fun getPostById(@PathVariable("id") id: Int) : Post

	@RequestMapping(value = ["/posts/{postId}/comments"])
	fun getCommentsByPostId(@PathVariable("postId") postId: Int) : Array<Comment>
}
