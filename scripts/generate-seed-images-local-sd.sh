#!/usr/bin/env bash
set -euo pipefail

config_file="${BTM_LOCAL_SD_CONFIG:-beat-the-machine-adapters/src/main/resources/local-stable-diffusion.yml}"

config_value() {
  ruby -ryaml -e \
    'value = ARGV[1].split(".").reduce(YAML.load_file(ARGV[0])) { |node, key| node.fetch(key) }; puts value' \
    "$config_file" "$1"
}

base_url="${BTM_IMAGE_LOCAL_SD_BASE_URL:-$(config_value btm.image.local-sd.base-url)}"
out_dir="${BTM_SEED_IMAGE_DIR:-beat-the-machine-adapters/src/main/resources/static/seed/images}"
steps="${BTM_IMAGE_LOCAL_SD_STEPS:-$(config_value btm.image.local-sd.steps)}"
width="${BTM_IMAGE_LOCAL_SD_WIDTH:-$(config_value btm.image.local-sd.width)}"
height="${BTM_IMAGE_LOCAL_SD_HEIGHT:-$(config_value btm.image.local-sd.height)}"
cfg_scale="${BTM_IMAGE_LOCAL_SD_CFG_SCALE:-$(config_value btm.image.local-sd.cfg-scale)}"
selected_ids="${BTM_SEED_IMAGE_IDS:-}"
prompt_prefix="${BTM_SEED_IMAGE_PROMPT_PREFIX:-${BTM_IMAGE_LOCAL_SD_PROMPT_PREFIX:-$(config_value btm.image.local-sd.prompt-prefix)}}"
prompt_suffix="${BTM_SEED_IMAGE_PROMPT_SUFFIX:-${BTM_IMAGE_LOCAL_SD_PROMPT_SUFFIX:-$(config_value btm.image.local-sd.prompt-suffix)}}"
negative_prompt="${BTM_SEED_IMAGE_NEGATIVE_PROMPT:-${BTM_IMAGE_LOCAL_SD_NEGATIVE_PROMPT:-$(config_value btm.image.local-sd.negative-prompt)}}"

mkdir -p "$out_dir"

curl -fsS "$base_url/sdapi/v1/options" >/dev/null

while IFS='|' read -r id prompt; do
  if [[ -n "$selected_ids" && ",$selected_ids," != *",$id,"* ]]; then
    continue
  fi

  out="$out_dir/$id.png"
  if [[ -s "$out" && "${BTM_SEED_IMAGE_FORCE:-false}" != "true" ]]; then
    printf 'skip %s\n' "$out"
    continue
  fi

  image_prompt="${prompt_prefix}${prompt}${prompt_suffix}"
  body="$(
    ruby -rjson -e \
      'puts JSON.generate(prompt: ARGV[0], negative_prompt: ARGV[1], steps: ARGV[2].to_i, width: ARGV[3].to_i, height: ARGV[4].to_i, cfg_scale: ARGV[5].to_f, batch_size: 1, n_iter: 1)' \
      "$image_prompt" "$negative_prompt" "$steps" "$width" "$height" "$cfg_scale"
  )"

  response="$(
    curl -fsS \
      -H 'Content-Type: application/json' \
      -X POST \
      --data "$body" \
      "$base_url/sdapi/v1/txt2img"
  )"

  ruby -rjson -rbase64 -e \
    'image = JSON.parse(STDIN.read).fetch("images").first or abort("txt2img returned no images"); File.binwrite(ARGV[0], Base64.decode64(image))' \
    "$out" <<<"$response"

  printf 'wrote %s\n' "$out"
done <<'EOF'
seed-easy-01|red robot
seed-easy-02|blue castle
seed-easy-03|sleepy dragon
seed-easy-04|golden guitar
seed-easy-05|paper boat
seed-easy-06|tiny volcano
seed-easy-07|green umbrella
seed-easy-08|moon rabbit
seed-easy-09|glass apple
seed-easy-10|orange submarine
seed-medium-01|astronaut eating noodles
seed-medium-02|dolphin wearing sunglasses
seed-medium-03|wizard painting stars
seed-medium-04|penguin riding scooter
seed-medium-05|pirate baking cupcakes
seed-medium-06|robot watering flowers
seed-medium-07|dragon reading newspaper
seed-medium-08|octopus playing chess
seed-medium-09|ghost holding lantern
seed-medium-10|cowboy fixing spaceship
seed-hard-01|clock tower made of pancakes
seed-hard-02|detective fox in rainy paris
seed-hard-03|mermaid repairing a bicycle
seed-hard-04|haunted library under the ocean
seed-hard-05|knight juggling crystal planets
seed-hard-06|garden party on mars
seed-hard-07|giant teapot in the desert
seed-hard-08|violin concert for mushrooms
seed-hard-09|train station inside a tree
seed-hard-10|ancient computer predicting weather
EOF
