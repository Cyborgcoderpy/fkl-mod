package com.beckytidus.girlfriendmod.dialogue;

import com.beckytidus.girlfriendmod.entity.BoyFriendEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * All keyword→response mappings for the boy companion.
 * Same topics as the girl but with a friendly bro tone — no romance at all.
 */
public class BoyDialogueData {

    private final List<Entry> entries = new ArrayList<>();

    private static final String[] CALL_RESPONSES = {
        "Yeah? What's up?",
        "You called?",
        "Right here, what do you need?",
        "Sup! Talk to me.",
        "Here! What's going on?",
        "Yeah yeah, I'm listening.",
        "What's the plan?"
    };

    public BoyDialogueData() {

        // ── Greetings ─────────────────────────────────────────────────────
        add(new String[]{"hi", "hello", "hey", "hiya"},
            new String[]{
                "Hey! Good to see you man.",
                "Yo! What are we doing today?",
                "Hey there! Ready for action?",
                "What's up! Let's get moving.",
                "Hey! Finally, I was getting bored."
            });

        // ── How are you ───────────────────────────────────────────────────
        add(new String[]{"how are you", "you okay", "alright", "how're you"},
            new String[]{
                "Doing great, ready to go!",
                "Pretty solid. Could eat though.",
                "Good! A bit restless, let's do something.",
                "Fine fine. You good?",
                "Never better. Let's move!"
            });

        // ── Food / hunger ─────────────────────────────────────────────────
        add(new String[]{"food", "hungry", "eat", "feed", "bread", "apple"},
            new String[]{
                "Oh nice, yeah I could eat.",
                "YES. Hand it over.",
                "Finally! I was starving.",
                "Food? Always yes.",
                "You're a lifesaver, I'm famished."
            });

        // ── Combat ────────────────────────────────────────────────────────
        add(new String[]{"fight", "attack", "defend", "protect", "danger", "enemy"},
            new String[]{
                "Let's go! I've got your back.",
                "Say less. On it.",
                "Nobody's touching you while I'm here.",
                "Let's handle this.",
                "Stay close, I'll take the front."
            });

        // ── Follow / wait ─────────────────────────────────────────────────
        add(new String[]{"follow", "come", "come here", "come with"},
            new String[]{
                "Right behind you!",
                "Let's go!",
                "On my way!",
                "Moving out!"
            });

        add(new String[]{"wait", "stay", "stay here", "don't move"},
            new String[]{
                "Got it, I'll wait here.",
                "Sure, I'll hold this spot.",
                "No problem, take your time.",
                "I'll be right here.",
                "Don't worry, I won't move.",
                "Understood, waiting here.",
                "I'll keep an eye on things.",
                "Go ahead, I'll hold down the fort."
            });

        // ── Exploration ───────────────────────────────────────────────────
        add(new String[]{"explore", "adventure", "travel", "go", "let's go"},
            new String[]{
                "Finally! Let's move!",
                "I've been waiting for this!",
                "YES. Lead the way!",
                "Adventure time! Let's see what's out there.",
                "Let's go find something interesting."
            });

        // ── Night / sleep ─────────────────────────────────────────────────
        add(new String[]{"night", "sleep", "bed", "tired", "rest"},
            new String[]{
                "Yeah I could use some rest honestly.",
                "Good call. Long day.",
                "Sleep sounds great right now.",
                "Finally, my legs are done.",
                "Rest up. We need to be sharp tomorrow."
            });

        // ── Hurt / health ─────────────────────────────────────────────────
        add(new String[]{"hurt", "health", "heal", "injured", "damage"},
            new String[]{
                "Took a few hits but I'm fine.",
                "Just a scratch. I'll walk it off.",
                "I've had worse. Keep going.",
                "Eh, I'll recover. Don't worry about me.",
                "I'm good. Focus on the mission."
            });

        // ── Boredom ───────────────────────────────────────────────────────
        add(new String[]{"bored", "nothing to do", "boring"},
            new String[]{
                "Same. Let's go find trouble.",
                "Then let's do something! I'm restless too.",
                "Bored? We should be building or exploring.",
                "Let's go mining or something."
            });

        // ── Sad / emotional ───────────────────────────────────────────────
        add(new String[]{"sad", "upset", "down", "miss you"},
            new String[]{
                "Hey, chin up. We'll figure it out.",
                "Don't sweat it. I'm right here.",
                "Come on, we've been through worse.",
                "It's gonna be okay man. Let's keep moving.",
                "I gotchu. Don't worry about it."
            });

        // ── Questions about him ────────────────────────────────────────────
        add(new String[]{"who are you", "your name", "what's your name"},
            new String[]{
                "Your best companion, obviously.",
                "The guy who keeps saving you, apparently.",
                "Does it matter? I'm here, aren't I?",
                "Your right-hand man. That's all you need to know."
            });

        add(new String[]{"favorite", "like most", "best thing"},
            new String[]{
                "Honestly? A good fight and then a good meal.",
                "Exploring new places. Always something new.",
                "When a plan actually works perfectly.",
                "That moment after clearing a dungeon. Pure satisfaction."
            });

        // ── Mobs ──────────────────────────────────────────────────────────
        add(new String[]{"creeper", "zombie", "skeleton", "monster", "mob", "spider"},
            new String[]{
                "Where?! I'll handle it.",
                "On it! Stay back.",
                "I see it. Leave it to me.",
                "Already moving. Don't get hit.",
                "Let's take it out fast."
            });

        // ── Thanks ────────────────────────────────────────────────────────
        add(new String[]{"thank you", "thanks", "appreciate"},
            new String[]{
                "Anytime, that's what I'm here for.",
                "Don't mention it.",
                "Of course. Always.",
                "No problem at all.",
                "Yep! Don't sweat it."
            });

        // ── Jokes ─────────────────────────────────────────────────────────
        add(new String[]{"joke", "funny", "laugh", "haha"},
            new String[]{
                "Why don't skeletons fight each other? They don't have the guts!",
                "I tried to come up with a joke about mining... but it was below me.",
                "What do you call a lazy creeper? An ex-ploder... wait that still works.",
                "Why did the Enderman cross the road? He teleported halfway and forgot why."
            });

        // ── Weather ───────────────────────────────────────────────────────
        add(new String[]{"rain", "sunny", "weather", "storm"},
            new String[]{
                "Rain's fine by me. Good cover.",
                "Perfect weather for staying in and crafting.",
                "Storm's coming. Let's find shelter.",
                "A bit of rain never stopped us."
            });

        // ── Returning after time away ──────────────────────────────────────
        add(new String[]{"back", "return", "returned", "i'm back"},
            new String[]{
                "Welcome back! What did I miss?",
                "There you are! Thought you got lost.",
                "Finally! I was starting to wonder.",
                "Good timing. Was about to go look for you."
            });
    }

    public void add(String[] keywords, String[] responses) {
        entries.add(new Entry(keywords, responses));
    }

    public String pickReply(String message, BoyFriendEntity bf, float roll) {
        if (message == null || message.isEmpty()) return null;
        String lower = message.toLowerCase(Locale.ROOT);

        if (bf.getHunger() < 5) {
            if (lower.contains("food") || lower.contains("eat") || lower.contains("hungry")) {
                return "Bro I'm really hungry... got anything to eat?";
            }
        }

        for (Entry entry : entries) {
            for (String kw : entry.keywords) {
                if (lower.contains(kw)) {
                    String[] resp = entry.responses;
                    return resp[(int)(roll * resp.length) % resp.length];
                }
            }
        }

        // Fallback
        String[] fallbacks = {
            "Yeah, makes sense.",
            "Tell me more.",
            "Noted.",
            "Interesting.",
            "Yep.",
            "What do you mean exactly?",
            "Go on.",
            "Hm. Interesting."
        };
        return fallbacks[(int)(roll * fallbacks.length)];
    }

    public String pickCallReply(float roll) {
        return CALL_RESPONSES[(int)(roll * CALL_RESPONSES.length) % CALL_RESPONSES.length];
    }

    public static class Entry {
        public final String[] keywords;
        public final String[] responses;
        public Entry(String[] k, String[] r) { keywords = k; responses = r; }
    }
}
