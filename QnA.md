What do you think are the greatest risk areas in completing this project?

1) I'm new to Android so learning the new platform could be an issue
2) The features I'm planning on could be overly optimistic and may be more difficult to implement than I think
3) Under estimating the time it could take to do things


What changes would you make to the data API if you were able to influence its design?

1) Add an "ID" to the listings provided by the API, without a unqiue ID it is very difficult to maintain a cache of this data,
   even by combining all the information provided by the API, you can not guarantee a listing is unique.
2) Combine the three APIs into one, it would decrease the amount of http requests needed to grab all of the information
3) Additional information about the cars lists, e.g. exterior, size, milage, and so on 


List a two or three features that you are unlikely to have time to implement that would add significant value to this app.

1) more advanced caching stratgies, and more optimized http requests. Given the available API caching data is very difficult
   since there is no way to update information (no unique id to differ between new and old listings). Although the API is static
   is shouldn't be assumed so. Having these in place could greatly improve performance
2) multipage results, I want to break the results up into multiple pages, this makes nagivating a large set of results easier

--

How long did this assignment take (in hours)?

~25 hours, a lot of it was prep time, reading up on the platform and experimenting with features

What was the hardest part?

1) working with the Android fragments/controls in some cases the behavior was not what I expected and it required some
tweaking to get what I wanted
2) communication between fragments/activities was a bit strange, I'm still not sure if callbacks were the best way to go
   but they seem to be used a lot


If you could go back and give yourself advice at the beginning of the project, what would it be?

I would tell my self to work on the application navigation first, figuring out the data processing wasn't as difficult
as getting desired behavior


Did you learn anything new?

I learned a lot about the Android platform


Do you feel that this assignment allowed you to showcase your abilities effectively?

more or less


Are there any Android-related skills that you weren't able to demonstrate in this excercise? If so, what are they?

stuff with intents, I never got to try these but they seemed interesting
