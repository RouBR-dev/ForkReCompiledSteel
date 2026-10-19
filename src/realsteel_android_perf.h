// realsteel - Android scheduling helpers (Mali / Adreno slow-motion fix).
//
// The game's logic advances one fixed step per presented frame, so whenever
// the guest thread misses its 16.7 ms slot the game runs in slow motion.
// On big.LITTLE SoCs (every Mali and Adreno phone) Android happily parks a
// busy game thread on a LITTLE core, or lets the renderer thread compete
// with it; both show up as slow motion. Raising the priority of the two
// threads that matter keeps them on the big cores.
#pragma once

#ifdef __ANDROID__
#include <sys/resource.h>
#include <sys/syscall.h>
#include <unistd.h>

#include <rex/logging.h>

namespace realsteel::android_perf {

// nice: -20 (most urgent) .. 19. Android lets apps go down to -8
// (THREAD_PRIORITY_URGENT_DISPLAY); lower values are clamped by the kernel.
inline void BoostThisThread(int nice, const char* what) {
  const pid_t tid = static_cast<pid_t>(syscall(SYS_gettid));
  if (setpriority(PRIO_PROCESS, static_cast<id_t>(tid), nice) == 0) {
    REXLOG_INFO("[perf] {} thread {} priority {}", what, static_cast<int>(tid), nice);
  } else {
    REXLOG_WARN("[perf] {} thread priority not changed", what);
  }
}

}  // namespace realsteel::android_perf
#else
namespace realsteel::android_perf {
inline void BoostThisThread(int, const char*) {}
}  // namespace realsteel::android_perf
#endif
