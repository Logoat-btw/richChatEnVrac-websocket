const UNICODE_ALPHANUM_WORD = /(?=[\p{L}\p{N}])[\p{L}\p{N}\p{Pd}\p{Pc}]*[\p{L}\p{N}]/ug;

function computeWordStats(messages = []) {
  const countsByWord = messages
    .map(txt => txt.toLowerCase())
    // Fix: Remove brackets around regex and handle null with '|| []'
    .flatMap(txt => txt.match(UNICODE_ALPHANUM_WORD) || [])
    .filter(word => word.length >= 3)
    .reduce((dico, word) => {
      dico[word] = (dico[word] ?? 0) + 1;
      return dico;
    }, {});

  return Object.entries(countsByWord)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)
    .map(([word, count]) => ({ word, count }));
}

function processMessage(msg) {
  const { roomId, messages } = msg.data;
  const stats = computeWordStats(messages);
  self.postMessage({ roomId, stats });
}

self.onmessage = processMessage;
