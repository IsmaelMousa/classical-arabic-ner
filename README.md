# Classical Arabic Named Entity Recognition

The *Maximum Entropy Markov* Model is a widely used sequence model, especially in information extraction and NER.
To be honest I did not write this project, as I don't write Java. My contribution was adding features (1 to 10) to improve model performance.

## Objective
The main objective is to create and add new features to the FeatureFactory.java file to improve the Arabic NER system using the Maximum Entropy Markov model in order to enhance the recognition of PERSON entities.

## Dataset

The `ANERcorp` dataset contains labeled data with words and their corresponding entity labels such as PERSON and O (non-entity).


```
وقال O
رئيس O
الاتحاد O
برند PERSON
جوتشولك PERSON 
عند O
إعلان O
آخر O
تقرير O
سنوي O
```

## Challenges

Several challenges are addressed in this task, but I will focus on the features that caught my attention the most.

Arabic entities can shift in meaning depending on the surrounding words, so I added features that considered adjacent words. For example, if a word came right after "الرئيس" (president), it was likely part of a political title, which the model needed to recognize. But I didn’t just look at the immediate previous word. I also considered the word before that because, in some cases, the context is more nuanced.

Take this example: "وصرح الرئيس الجديد دونالد ترامب" ("The new president Donald Trump stated"). Here, the word directly before "دونالد ترامب" is "الجديد" (new), not "الرئيس" (president). If I only checked the immediate word, I would have missed the context. But by looking at the second word before, I could catch the fact that "الرئيس" was still the key context, helping me achieve the right condition.

Additionally, I paid attention to the verbs that came before the target word. For example, in "وقال بشار الأسد رئيس جمهورية سوريا" ("Bashar al-Assad, President of the Syrian Arab Republic, stated"), the verb "وقال" (said) before "بشار الأسد" (Bashar al-Assad) signals that the following word is likely to be a person, so it was crucial for identifying "بشار الأسد" as a PERSON entity.

Context also mattered, so I included both the previous and next words to capture this. Sometimes, the surrounding words can drastically change the meaning of the entity.

I also considered nationalities, such as in "أعلن اللاعب البرازيلي نيمار" ("Brazilian player Neymar announced"), where the nationality "البرازيلي" (Brazilian) gave important clues for identifying the person "نيمار" (Neymar).

This was an ongoing process of tweaking and experimenting to avoid overfitting or underfitting. In the end, these additional features significantly improved the recognition of PERSON entities, which was the core challenge.

## Results
The following table presents a comparison between the baseline performance and the improved performance.

The metrics used to evaluate the system's performance include precision, recall, and F1 score:

|    Metric     | Baseline Performance | Improved Performance |
|:-------------:|:--------------------:|:--------------------:|
| **Precision** |        0.569         |        0.623         |
|  **Recall**   |        0.189         |        0.437         |
|    **F1**     |        0.284         |        0.514         |
