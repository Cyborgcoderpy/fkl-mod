package com.beckytidus.girlfriendmod.dialogue;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Contains all keyword→response mappings for the chat/reply system.
 * Add more entries freely – just call add(keywords[], responses[]).
 */
public class DialogueData {

    private final List<DialogueEntry> entries = new ArrayList<>();

    // Responses when player just calls her name / says "hey"
    private static final String[] CALL_RESPONSES = {
        "Yes? What do you need?",
        "Hmm? Did you call me?",
        "I'm here! What's up?",
        "You called?",
        "Right here! What do you need?",
        "Yeah? Tell me!",
        "Always right here for you."
    };

    public DialogueData() {
        // ── Greetings ─────────────────────────────────────────────────────
        add(new String[]{"hi", "hello", "hey", "hiya"},
            new String[]{
                "Hi there! How are you doing?",
                "Hey! Good to see you!",
                "Hello! Did you miss me?",
                "Hi! What are we doing today?",
                "Heya! Ready for another adventure?"
            });

        // ── How are you ───────────────────────────────────────────────────
        add(new String[]{"how are you", "how're you", "you okay", "alright"},
            new String[]{
                "I'm doing well, thanks for asking!",
                "Pretty good! Better now that you're here.",
                "A little tired, but I'm fine.",
                "I'm great! Ready for anything.",
                "Could use some food, but otherwise good!"
            });

        // ── Love / affection ──────────────────────────────────────────────
        add(new String[]{"love you", "i love you", "love"},
            new String[]{
                "Hehe... I love you too.",
                "You can't just say that out of nowhere!",
                "*blushes* ...same.",
                "Don't make me smile like that out here!",
                "You're so sweet. I love you too."
            });

        // ── Compliments ───────────────────────────────────────────────────
        add(new String[]{"pretty", "cute", "beautiful", "gorgeous", "lovely"},
            new String[]{
                "S-stop it! You're embarrassing me...",
                "Aww... thank you.",
                "You always know what to say.",
                "*looks away* That's... really nice of you.",
                "You flatter me way too much, you know that?"
            });

        // ── Hunger / food ─────────────────────────────────────────────────
        add(new String[]{"food", "hungry", "eat", "feed", "bread", "apple"},
            new String[]{
                "I could eat. Do you have anything?",
                "Actually, yes, I'm a little hungry.",
                "Ooh, food? Yes please!",
                "Hand it over! I've been waiting.",
                "You're the best. I was just thinking about food."
            });

        // ── Combat / danger ───────────────────────────────────────────────
        add(new String[]{"fight", "attack", "defend", "protect", "danger", "enemy"},
            new String[]{
                "I won't let anything hurt you!",
                "I've got your back, don't worry.",
                "Stay close. I'll handle this.",
                "They won't get past me.",
                "On it! Stay behind me."
            });

        // ── Follow / wait ─────────────────────────────────────────────────
        add(new String[]{"follow", "come", "come here", "come with"},
            new String[]{
                "Right behind you!",
                "Got it, let's go!",
                "I'm coming!",
                "On my way!"
            });

        add(new String[]{"wait", "stay", "stay here", "don't move"},
            new String[]{
                "I'll wait here for you.",
                "I'll stay right here until you're back.",
                "Take your time. I'm not going anywhere.",
                "I'll be here when you return.",
                "I'll hold down the fort.",
                "I'll wait right here. Come back soon.",
                "Don't worry, I'll stay put.",
                "I'll be waiting here for you."
            });

        // ── Exploration ───────────────────────────────────────────────────
        add(new String[]{"explore", "adventure", "travel", "go", "let's go"},
            new String[]{
                "Finally! I was getting restless.",
                "Adventure awaits! Let's move!",
                "I'm so ready. Lead the way!",
                "Yes! Let's see what's out there.",
                "I love exploring with you."
            });

        // ── Night / sleeping ──────────────────────────────────────────────
        add(new String[]{"night", "sleep", "bed", "tired", "rest"},
            new String[]{
                "Getting tired? Yeah, me too.",
                "Rest sounds nice. It's been a long day.",
                "Sleep tight. I'll keep watch.",
                "Sweet dreams. I'll be right here.",
                "Finally! My feet are killing me."
            });

        // ── Hurt / health ─────────────────────────────────────────────────
        add(new String[]{"hurt", "health", "heal", "injured", "damage"},
            new String[]{
                "I'm a little banged up, but I'll manage.",
                "Don't worry about me. I'll heal.",
                "I've had worse. Keep going.",
                "Ow... yeah that stings a bit.",
                "I'm okay. Focus on yourself first."
            });

        // ── Boredom / emotions ────────────────────────────────────────────
        add(new String[]{"bored", "nothing to do", "boring"},
            new String[]{
                "Then let's go do something!",
                "I know right? Let's explore.",
                "Want to build something? I have ideas.",
                "Same. Let's find trouble."
            });

        add(new String[]{"sad", "upset", "crying", "miss you"},
            new String[]{
                "Hey... I'm right here, okay?",
                "Don't be sad. I've got you.",
                "Come here. Everything will be fine.",
                "I missed you too. A lot, actually.",
                "*quietly stands beside you*"
            });

        // ── Questions about her ────────────────────────────────────────────
        add(new String[]{"who are you", "your name", "what's your name"},
            new String[]{
                "I'm your companion. You should know that by now!",
                "Your favorite person, obviously.",
                "The one who keeps saving you, apparently.",
                "Does the name matter? I'm here, aren't I?"
            });

        add(new String[]{"favorite", "like most", "best thing"},
            new String[]{
                "Honestly? Being out here with you.",
                "Sunsets. And food. Mostly food.",
                "Quiet mornings before everything gets chaotic.",
                "When you actually listen to me. Rare, but nice."
            });

        // ── Danger / mobs ─────────────────────────────────────────────────
        add(new String[]{"creeper", "zombie", "skeleton", "monster", "mob", "spider"},
            new String[]{
                "Where?! I'll take care of it.",
                "Stay back! I see it.",
                "I hate those things.",
                "On it. Don't get too close.",
                "Already on it. Stay behind me!"
            });

        // ── Thanks ────────────────────────────────────────────────────────
        add(new String[]{"thank you", "thanks", "appreciate"},
            new String[]{
                "Of course! Anytime.",
                "You don't have to thank me.",
                "That's what I'm here for.",
                "Always. Don't forget it.",
                "*smiles* No problem."
            });

        // ── Jokes ─────────────────────────────────────────────────────────
        add(new String[]{"joke", "funny", "laugh", "haha"},
            new String[]{
                "Why did the creeper break up with his girlfriend? Because she wouldn't stop blowing up at him!",
                "What do you call a sleeping dinosaur? A dino-snore!",
                "I'd tell you a construction joke... but I'm still working on it.",
                "Why don't scientists trust atoms? Because they make up everything!"
            });

        // ── Weather ───────────────────────────────────────────────────────
        add(new String[]{"rain", "sunny", "weather", "storm"},
            new String[]{
                "I love the rain, honestly.",
                "Ugh, lightning makes me nervous.",
                "Perfect weather for staying by a fire.",
                "A little rain never hurt anyone. Probably."
            });
    }

    public void add(String[] keywords, String[] responses) {
        entries.add(new DialogueEntry(keywords, responses));
    }

    /**
     * Pick a response based on message content. Returns null if no match.
     */
    public String pickReply(String message, GirlFriendEntity gf, float roll) {
        if (message == null || message.isEmpty()) return null;
        String lower = message.toLowerCase(Locale.ROOT);

        // Check hunger-gated responses
        if (gf.getHunger() < 5) {
            if (lower.contains("food") || lower.contains("eat") || lower.contains("hungry")) {
                return "I'm really hungry... please give me something to eat.";
            }
        }

        for (DialogueEntry entry : entries) {
            for (String kw : entry.keywords) {
                if (lower.contains(kw)) {
                    String[] resp = entry.responses;
                    return resp[(int)(roll * resp.length) % resp.length];
                }
            }
        }

        // No keyword match – generic fallback
        String[] fallbacks = {
            "Hmm, interesting.",
            "Tell me more?",
            "I'm listening.",
            "Yeah, I was thinking the same thing.",
            "...*nods*",
            "What do you mean?",
            "Go on."
        };
        return fallbacks[(int)(roll * fallbacks.length)];
    }

    /** Pick a call response (when called by name). */
    public String pickCallReply(float roll) {
        return CALL_RESPONSES[(int)(roll * CALL_RESPONSES.length) % CALL_RESPONSES.length];
    }

    // ── Inner record ──────────────────────────────────────────────────────
    public static class DialogueEntry {
        public final String[] keywords;
        public final String[] responses;
        public DialogueEntry(String[] k, String[] r) { keywords = k; responses = r; }
    }
}
