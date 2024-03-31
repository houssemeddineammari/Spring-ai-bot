package com.hea.springaitest.web;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class SpringAiTestController {

	@Value("${spring.ai.openai.api-key}")
	private String apiKey;

	@Autowired
	private ChatClient chatClient;

	@GetMapping("/ping")
	public String sendMessage(String request) {
		String response = chatClient.call(request);
		return response;
	}

	@GetMapping("/movie")
	public Map findBestMovie(@RequestParam(name = "category", defaultValue = "action") String category,
			@RequestParam(name = "year", defaultValue = "2019") String year)
			throws JsonMappingException, JsonProcessingException {

		OpenAiApi api = new OpenAiApi(apiKey);

		OpenAiChatOptions options = OpenAiChatOptions.builder().withModel("gpt-3.5-turbo").withTemperature(0F)
				.withMaxTokens(2000).build();

		OpenAiChatClient chatClient = new OpenAiChatClient(api, options);

		SystemPromptTemplate promptTemplate = new SystemPromptTemplate("""
				I need you to give me the best movie on the given category : {category}
				on the given year : {year}.
				the output should be in json format including the following fields :
				- category<The given category>
				- year<The given year>
				- title<The title of the movie>
				- producer<The producer of the movie>
				- actors<A list of main actors of the movie>
				- summary<A very small summary of the movie>
				""");

		Prompt prompt = promptTemplate.create(Map.of("category", category, "year", year));
		ChatResponse response = chatClient.call(prompt);

		String content = response.getResult().getOutput().getContent();

		return new ObjectMapper().readValue(content, Map.class);
	}

	/*
	@GetMapping("/sentiment-analyses")
	public Map sentimentAnalysis(String review) throws JsonMappingException, JsonProcessingException {
		OpenAiApi openAiApi = new OpenAiApi(apiKey);
		OpenAiChatOptions options = OpenAiChatOptions.builder().withModel("gpt-4").withTemperature(0F)
				.withMaxTokens(2000).build();
		OpenAiChatClient openAiChatClient = new OpenAiChatClient(openAiApi, options);

		String systemMessageText = """
				Perform aspect based sentiment analysis on laptop reviews presented in the input delimited by triple backticks, that is: ```
				In each review there might be one or more of the following aspects: screen, keyboard, and mousepad
				For each review presented as input:
				- Identify if there are any of the 3 aspects (screen, keyboard, mousepad) present in the review.
				- Assign a sentiment polarity (positive, negative or neutral) for each aspect
				- Arrange your response a Json object with the following headers:
				- category:[list of aspects]
				- polarity:[list of corresponding polarities for each aspect]
				""";

		SystemMessage systemMessage = new SystemMessage(systemMessageText);
		UserMessage userMessage = new UserMessage("\"\"\"review\"\"\"");
		Prompt zeroShotPrompt = new Prompt(List.of(systemMessage, userMessage));
		ChatResponse response = openAiChatClient.call(zeroShotPrompt);
		String content =  response.getResult().getOutput().getContent();
		return new ObjectMapper().readValue(content, Map.class);

	}
*/
}
